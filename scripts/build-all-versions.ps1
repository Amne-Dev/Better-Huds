param(
	[string]$Task = "build",
	[string]$ProfilesPath = "",
	[string]$OutputRoot = "build/multi-version",
	[string]$OnlyMinecraftVersion = "",
	[switch]$BuildByCompatGroup,
	[switch]$StopOnError,
	[switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Load-Profiles {
	param([string]$Path)

	if (-not (Test-Path $Path)) {
		throw "Profiles file not found: $Path"
	}

	$raw = Get-Content -Raw -Path $Path
	$profiles = $raw | ConvertFrom-Json
	if ($null -eq $profiles) {
		throw "Profiles file is empty: $Path"
	}

	$valid = @()
	foreach ($profile in @($profiles)) {
		$minecraftVersion = [string]$profile.minecraft_version
		$fabricApiVersion = [string]$profile.fabric_api_version
		if ([string]::IsNullOrWhiteSpace($minecraftVersion) -or [string]::IsNullOrWhiteSpace($fabricApiVersion)) {
			continue
		}
		$loaderVersion = if ([string]::IsNullOrWhiteSpace([string]$profile.loader_version)) { "0.19.3" } else { [string]$profile.loader_version }
		$name = if ([string]::IsNullOrWhiteSpace([string]$profile.name)) { $minecraftVersion } else { [string]$profile.name }
		$compatGroup = if ([string]::IsNullOrWhiteSpace([string]$profile.compat_group)) { $minecraftVersion } else { [string]$profile.compat_group }
		$minecraftDependency = if ([string]::IsNullOrWhiteSpace([string]$profile.minecraft_dependency)) { $minecraftVersion } else { [string]$profile.minecraft_dependency }
		$valid += [PSCustomObject]@{
			name = $name
			minecraft_version = $minecraftVersion
			fabric_api_version = $fabricApiVersion
			loader_version = $loaderVersion
			compat_group = $compatGroup
			minecraft_dependency = $minecraftDependency
		}
	}

	if ($valid.Count -eq 0) {
		throw "No valid profiles found in: $Path"
	}

	return $valid
}

function Collapse-ProfilesByGroup {
	param([array]$Profiles)

	if ($Profiles.Count -eq 0) {
		return @()
	}

	$grouped = [ordered]@{}
	foreach ($profile in @($Profiles)) {
		$groupKey = [string]$profile.compat_group
		if ([string]::IsNullOrWhiteSpace($groupKey)) {
			$groupKey = [string]$profile.minecraft_version
		}
		if (-not $grouped.Contains($groupKey)) {
			$grouped[$groupKey] = $profile
			continue
		}

		$current = $grouped[$groupKey]
		if ((Compare-MinecraftVersion -Left ([string]$profile.minecraft_version) -Right ([string]$current.minecraft_version)) -lt 0) {
			$grouped[$groupKey] = $profile
		}
	}

	return @($grouped.Values)
}

function Get-VersionParts {
	param([string]$Version)

	if ([string]::IsNullOrWhiteSpace($Version)) {
		return @()
	}

	$matches = [regex]::Matches($Version, "\d+")
	$parts = @()
	foreach ($m in $matches) {
		$parts += [int]$m.Value
	}
	return $parts
}

function Compare-MinecraftVersion {
	param(
		[string]$Left,
		[string]$Right
	)

	$leftParts = Get-VersionParts -Version $Left
	$rightParts = Get-VersionParts -Version $Right
	$max = [Math]::Max($leftParts.Count, $rightParts.Count)
	for ($i = 0; $i -lt $max; $i++) {
		$l = if ($i -lt $leftParts.Count) { $leftParts[$i] } else { 0 }
		$r = if ($i -lt $rightParts.Count) { $rightParts[$i] } else { 0 }
		if ($l -lt $r) { return -1 }
		if ($l -gt $r) { return 1 }
	}
	return 0
}

function Clear-LibsJars {
	param([string]$RepoRoot)

	$libsDir = Join-Path $RepoRoot "build/libs"
	if (-not (Test-Path $libsDir)) {
		return
	}

	Get-ChildItem -Path $libsDir -Filter "*.jar" -File | Remove-Item -Force -ErrorAction SilentlyContinue
}

function Export-Artifacts {
	param(
		[string]$RepoRoot,
		[string]$OutputRootPath,
		[string]$OutputKey
	)

	$libsDir = Join-Path $RepoRoot "build/libs"
	if (-not (Test-Path $libsDir)) {
		return @()
	}

	$versionSafe = $OutputKey -replace '[^0-9A-Za-z._-]', '_'
	$targetDir = Join-Path $RepoRoot $OutputRootPath
	$targetDir = Join-Path $targetDir $versionSafe
	New-Item -ItemType Directory -Force -Path $targetDir | Out-Null

	$files = Get-ChildItem -Path $libsDir -Filter "*.jar" -File
	$copied = @()
	foreach ($file in $files) {
		$destination = Join-Path $targetDir $file.Name
		Copy-Item -Path $file.FullName -Destination $destination -Force
		$copied += $destination
	}

	return $copied
}

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$effectiveProfilesPath = if ([string]::IsNullOrWhiteSpace($ProfilesPath)) {
	Join-Path $PSScriptRoot "build-profiles.json"
} else {
	$ProfilesPath
}

$profiles = Load-Profiles -Path $effectiveProfilesPath
if (-not [string]::IsNullOrWhiteSpace($OnlyMinecraftVersion)) {
	$profiles = @($profiles | Where-Object { $_.minecraft_version -eq $OnlyMinecraftVersion })
	if ($profiles.Count -eq 0) {
		throw "No profile found for minecraft version: $OnlyMinecraftVersion"
	}
} elseif ($BuildByCompatGroup) {
	$profiles = Collapse-ProfilesByGroup -Profiles $profiles
}

$gradlew = Join-Path $repoRoot "gradlew.bat"
if (-not (Test-Path $gradlew)) {
	throw "gradlew.bat not found at: $gradlew"
}

Write-Host ""
Write-Host "Task:          $Task" -ForegroundColor DarkCyan
Write-Host "Profiles file: $effectiveProfilesPath" -ForegroundColor DarkCyan
Write-Host "Output root:   $OutputRoot" -ForegroundColor DarkCyan
Write-Host "Mode:          $(if ($BuildByCompatGroup) { 'one build per compat group' } else { 'each profile/version' })" -ForegroundColor DarkCyan
if (-not [string]::IsNullOrWhiteSpace($OnlyMinecraftVersion)) {
	Write-Host "Only version:  $OnlyMinecraftVersion" -ForegroundColor DarkCyan
}
Write-Host "Count:         $($profiles.Count)" -ForegroundColor DarkCyan
Write-Host ""

$results = @()

for ($i = 0; $i -lt $profiles.Count; $i++) {
	$profile = $profiles[$i]
	$index = $i + 1
	$total = $profiles.Count
	$minecraftVersion = $profile.minecraft_version
	$fabricApiVersion = $profile.fabric_api_version
	$loaderVersion = $profile.loader_version
	$compatGroup = $profile.compat_group
	$minecraftDependency = $profile.minecraft_dependency

	Write-Host ("[{0}/{1}] {2}" -f $index, $total, $profile.name) -ForegroundColor Cyan
	Write-Host "  Minecraft:     $minecraftVersion"
	Write-Host "  Fabric API:    $fabricApiVersion"
	Write-Host "  Fabric Loader: $loaderVersion"
	Write-Host "  Compat group:  $compatGroup"
	Write-Host "  MC depends:    $minecraftDependency"

	$gradleArgs = @(
		"-Pminecraft_version=$minecraftVersion"
		"-Pfabric_api_version=$fabricApiVersion"
		"-Ploader_version=$loaderVersion"
		"-Pmc_compat_group=$compatGroup"
		"-Pmc_dependency_range=$minecraftDependency"
		"-Pbuild_profile_name=$($profile.name)"
		"-Pbuild_version_label=$minecraftVersion"
		$Task
	)

	Write-Host "  Running: .\gradlew.bat $($gradleArgs -join ' ')" -ForegroundColor Yellow

	$start = Get-Date
	$exitCode = 0
	$copiedArtifacts = @()
	$outputKey = if ($BuildByCompatGroup) { $compatGroup } else { $minecraftVersion }

	if ($DryRun) {
		Write-Host "  Dry run enabled; Gradle was not executed." -ForegroundColor Yellow
	} else {
		Clear-LibsJars -RepoRoot $repoRoot
		& $gradlew @gradleArgs
		$exitCode = $LASTEXITCODE
		if ($exitCode -eq 0) {
			$copiedArtifacts = Export-Artifacts -RepoRoot $repoRoot -OutputRootPath $OutputRoot -OutputKey $outputKey
		}
	}

	$duration = [int]((Get-Date) - $start).TotalSeconds
	$status = if ($exitCode -eq 0) { "PASS" } else { "FAIL" }
	$artifactDir = if ($copiedArtifacts.Count -gt 0) { Split-Path -Parent $copiedArtifacts[0] } else { "" }

	$results += [PSCustomObject]@{
		version = $minecraftVersion
		group = $compatGroup
		status = $status
		exit_code = $exitCode
		duration_s = $duration
		artifact_count = $copiedArtifacts.Count
		artifact_dir = $artifactDir
	}

	if ($exitCode -eq 0) {
		Write-Host ("  Result: {0} ({1}s)" -f $status, $duration) -ForegroundColor Green
		if ($copiedArtifacts.Count -gt 0) {
			Write-Host ("  Exported: {0} artifact(s) to {1}" -f $copiedArtifacts.Count, (Split-Path -Parent $copiedArtifacts[0])) -ForegroundColor DarkGreen
		}
	} else {
		Write-Host ("  Result: {0} ({1}s, exit {2})" -f $status, $duration, $exitCode) -ForegroundColor Red
		if ($StopOnError) {
			Write-Host "  Stopping due to -StopOnError." -ForegroundColor Red
			break
		}
	}
	Write-Host ""
}

Write-Host "Summary" -ForegroundColor Cyan
foreach ($result in $results) {
	$line = ("  {0,-8} [{1}] {2,-4} ({3}s, exit {4}, artifacts {5})" -f $result.version, $result.group, $result.status, $result.duration_s, $result.exit_code, $result.artifact_count)
	if ($result.status -eq "PASS") {
		Write-Host $line -ForegroundColor Green
	} else {
		Write-Host $line -ForegroundColor Red
	}
}

$failed = @($results | Where-Object { $_.status -eq "FAIL" })
if ($failed.Count -gt 0) {
	exit 1
}

exit 0
