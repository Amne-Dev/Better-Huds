param(
	[string]$Task = "runClient",
	[string]$MinecraftVersion = "",
	[string]$FabricApiVersion = "",
	[string]$LoaderVersion = "",
	[int]$Profile = 0,
	[switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Read-Required {
	param(
		[string]$Prompt,
		[string]$DefaultValue = ""
	)

	while ($true) {
		$fullPrompt = if ([string]::IsNullOrWhiteSpace($DefaultValue)) { $Prompt } else { "$Prompt [$DefaultValue]" }
		$value = Read-Host $fullPrompt
		if ([string]::IsNullOrWhiteSpace($value)) {
			if (-not [string]::IsNullOrWhiteSpace($DefaultValue)) {
				return $DefaultValue
			}
			continue
		}
		return $value.Trim()
	}
}

function Load-Profiles {
	param([string]$Path)

	if (-not (Test-Path $Path)) {
		return @()
	}

	try {
		$raw = Get-Content -Raw -Path $Path
		$profiles = $raw | ConvertFrom-Json
		if ($null -eq $profiles) {
			return @()
		}
		return @($profiles)
	} catch {
		Write-Warning "Failed to read $Path. Falling back to custom input."
		return @()
	}
}

function Select-Profile {
	param([array]$Profiles)

	Write-Host ""
	Write-Host "Select target Minecraft version profile:" -ForegroundColor Cyan
	for ($i = 0; $i -lt $Profiles.Count; $i++) {
		$p = $Profiles[$i]
		Write-Host ("[{0}] {1}" -f ($i + 1), $p.name)
	}
	Write-Host ("[{0}] Custom" -f ($Profiles.Count + 1))
	Write-Host ""

	while ($true) {
		$choiceRaw = Read-Host "Enter choice number"
		[int]$choice = 0
		if (-not [int]::TryParse($choiceRaw, [ref]$choice)) {
			continue
		}
		if ($choice -ge 1 -and $choice -le ($Profiles.Count + 1)) {
			return $choice
		}
	}
}

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$minecraftVersion = $MinecraftVersion
$fabricApiVersion = $FabricApiVersion
$loaderVersion = $LoaderVersion

$profilesPath = Join-Path $PSScriptRoot "mc-profiles.json"
$profiles = Load-Profiles -Path $profilesPath

if (-not [string]::IsNullOrWhiteSpace($minecraftVersion) -or -not [string]::IsNullOrWhiteSpace($fabricApiVersion)) {
	$minecraftVersion = Read-Required -Prompt "Minecraft version (e.g. 1.21.11)" -DefaultValue $minecraftVersion
	$fabricApiVersion = Read-Required -Prompt "Fabric API version (e.g. 0.141.3+1.21.11)" -DefaultValue $fabricApiVersion
	$loaderVersion = Read-Required -Prompt "Fabric loader version (e.g. 0.18.4)" -DefaultValue $(if ([string]::IsNullOrWhiteSpace($loaderVersion)) { "0.18.4" } else { $loaderVersion })
} elseif ($Profile -gt 0 -and $Profile -le $profiles.Count) {
	$selected = $profiles[$Profile - 1]
	$minecraftVersion = Read-Required -Prompt "Minecraft version (e.g. 1.21.11)" -DefaultValue $selected.minecraft_version
	$fabricApiVersion = Read-Required -Prompt "Fabric API version (e.g. 0.141.3+1.21.11)" -DefaultValue $selected.fabric_api_version
	$loaderVersion = Read-Required -Prompt "Fabric loader version (e.g. 0.18.4)" -DefaultValue $(if ([string]::IsNullOrWhiteSpace($selected.loader_version)) { "0.18.4" } else { [string]$selected.loader_version })
} else {
	$choice = Select-Profile -Profiles $profiles
	if ($choice -le $profiles.Count) {
		$selected = $profiles[$choice - 1]
		$minecraftVersion = Read-Required -Prompt "Minecraft version (e.g. 1.21.11)" -DefaultValue $selected.minecraft_version
		$fabricApiVersion = Read-Required -Prompt "Fabric API version (e.g. 0.141.3+1.21.11)" -DefaultValue $selected.fabric_api_version
		$loaderVersion = Read-Required -Prompt "Fabric loader version (e.g. 0.18.4)" -DefaultValue $(if ([string]::IsNullOrWhiteSpace($selected.loader_version)) { "0.18.4" } else { [string]$selected.loader_version })
	} else {
		$minecraftVersion = Read-Required -Prompt "Minecraft version (e.g. 1.21.11)"
		$fabricApiVersion = Read-Required -Prompt "Fabric API version (e.g. 0.141.3+1.21.11)"
		$loaderVersion = Read-Required -Prompt "Fabric loader version (e.g. 0.18.4)" -DefaultValue "0.18.4"
	}
}

Write-Host ""
Write-Host "Task:            $Task" -ForegroundColor DarkCyan
Write-Host "Minecraft:       $minecraftVersion" -ForegroundColor DarkCyan
Write-Host "Fabric API:      $fabricApiVersion" -ForegroundColor DarkCyan
Write-Host "Fabric Loader:   $loaderVersion" -ForegroundColor DarkCyan
Write-Host ""

$gradlew = Join-Path $repoRoot "gradlew.bat"
if (-not (Test-Path $gradlew)) {
	throw "gradlew.bat not found at: $gradlew"
}

$gradleArgs = @(
	"-Pminecraft_version=$minecraftVersion"
	"-Pfabric_api_version=$fabricApiVersion"
	"-Ploader_version=$loaderVersion"
	$Task
)

Write-Host "Running: .\gradlew.bat $($gradleArgs -join ' ')" -ForegroundColor Yellow

if ($DryRun) {
	Write-Host "Dry run enabled; Gradle was not executed." -ForegroundColor Yellow
	exit 0
}

& $gradlew @gradleArgs
exit $LASTEXITCODE
