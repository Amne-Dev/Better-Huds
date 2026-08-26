# Better Huds

Better Huds is a modular Fabric HUD mod with draggable widgets, profiles, and per-widget customization.

## Documentation

- API reference: `docs/API.md`
- User wiki: `docs/WIKI.md`
- Modrinth listing text: `docs/MODRINTH_DESCRIPTION.md`
- Main extension point: `amdev.bh.api.BetterHudsApi`

## Supported Versions

- Supported build/runtime targets: `1.21`, `1.21.8`, `1.21.11`, `26.1`, and `26.2`
- Each generated jar declares its exact Minecraft target in `fabric.mod.json`.

## Quick API Start

Other mods can register custom Better Huds widgets at runtime.

### 1) Declare Better Huds as a dependency

In your `fabric.mod.json`, add Better Huds in `depends`:

```json
{
  "depends": {
    "better-huds": "*"
  },
  "entrypoints": {
    "better-huds": [
      "com.example.mod.MyBetterHudsEntrypoint"
    ]
  }
}
```

### 2) Implement the Better Huds entrypoint

```java
package com.example.mod;

import amdev.bh.api.BetterHudsApi;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import amdev.bh.config.BetterHudsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class MyBetterHudsEntrypoint implements BetterHudsApi.WidgetEntrypoint {
	@Override
	public void register(BetterHudsApi.WidgetRegistrar registrar) {
		registrar.register(new MyWidget());
	}

	private static final class MyWidget implements HudWidget {
		@Override
		public String id() {
			return "my_widget";
		}

		@Override
		public Component displayName() {
			return Component.translatable("widget.mymod.my_widget");
		}

		@Override
		public int getWidth(Minecraft client) {
			return 100;
		}

		@Override
		public int getHeight(Minecraft client) {
			return 14;
		}

		@Override
		public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
			graphics.drawString(client.font, "Hello", x, y, widgetConfig.textColor, false);
		}
	}
}
```

For complete API behavior (lifecycle, sizing rules, settings storage, performance guidance, and troubleshooting), see `docs/API.md`.

## Version Launcher Script

Use the interactive launcher to pick the Minecraft/Fabric versions before running Gradle:

- Windows batch wrapper:
  - `.\run-version.bat`
- PowerShell directly:
  - `.\scripts\run-version.ps1`

Default task is `runClient`. You can pass a different task:

- `.\run-version.bat build`
- `.\scripts\run-version.ps1 -Task runServer`

Profiles are stored in `scripts/mc-profiles.json`. The launcher only offers the five supported targets.

Build one version directly with Gradle:

- `.\gradlew.bat -Pminecraft_version=26.2 -Pfabric_api_version=0.158.0+26.2 -Ploader_version=0.19.3 build`

## Build All Versions Script

Use this to build every configured version in one command.

- Windows batch wrapper:
  - `.\build-all-versions.bat`
- PowerShell directly:
  - `.\scripts\build-all-versions.ps1`

Default task is `build` and versions are read from `scripts/build-profiles.json`.
Each profile can optionally define:

- `compat_group` (used to group output folders and metadata)
- `minecraft_dependency` (written into `fabric.mod.json` -> `depends.minecraft`)

By default, the script builds **every profile entry** (every configured Minecraft version).

Useful flags:

- `-Task jar`
- `-BuildByCompatGroup` (collapse to one build per `compat_group`, using the oldest version in each group)
- `-StopOnError`
- `-DryRun`
- `-OutputRoot build/multi-version`
- `-OnlyMinecraftVersion 26.1`
- `-ProfilesPath .\scripts\build-profiles.json`

Artifacts are exported to `build/multi-version/<minecraft-version>/` by default.
When using `-BuildByCompatGroup`, artifacts are exported to `build/multi-version/<compat-group>/`.
Each jar embeds build metadata in `fabric.mod.json` (`custom.build`) and uses a version/build label like `1.1+mc1.21.11`.
