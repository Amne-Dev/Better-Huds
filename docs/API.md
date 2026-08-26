# Better Huds API Reference

This document describes how to integrate custom widgets into Better Huds.

## Scope

The public extension surface is:

- `amdev.bh.api.BetterHudsApi.WidgetEntrypoint`
- `amdev.bh.api.BetterHudsApi.WidgetRegistrar`
- `amdev.bh.hud.HudWidget`

## Version Target

- Runtime targets maintained in this repository: Minecraft `1.21.8`, `1.21.11`, `26.1`, and `26.2` (`fabric.mod.json`)
- Each target has its own entry in `scripts/build-profiles.json` and produces a version-specific jar.
- API is intentionally small and stable, but this is still a mod API, so verify behavior when updating Better Huds versions.

## Integration Flow

1. Fabric loads Better Huds client entrypoint.
2. Better Huds discovers all `better-huds` entrypoints from other mods.
3. Better Huds calls each `WidgetEntrypoint#register`.
4. Entrypoints register one or more `HudWidget` implementations.
5. Registered widgets are included in HUD rendering, HUD editor, widget settings grid (generic appearance controls), and profile save/load.

## Setup

### 1) Add dependency

In your `fabric.mod.json`:

```json
{
  "depends": {
    "better-huds": "*"
  }
}
```

### 2) Add Better Huds entrypoint

```json
{
  "entrypoints": {
    "better-huds": [
      "com.example.mod.MyBetterHudsEntrypoint"
    ]
  }
}
```

### 3) Implement entrypoint

```java
package com.example.mod;

import amdev.bh.api.BetterHudsApi;
import amdev.bh.hud.HudWidget;
import net.minecraft.network.chat.Component;

public final class MyBetterHudsEntrypoint implements BetterHudsApi.WidgetEntrypoint {
	@Override
	public void register(BetterHudsApi.WidgetRegistrar registrar) {
		registrar.register(new ExampleWidget());
	}

	private static final class ExampleWidget implements HudWidget {
		@Override
		public String id() {
			return "example_widget";
		}

		@Override
		public Component displayName() {
			return Component.translatable("widget.mymod.example_widget");
		}

		@Override
		public int getWidth(net.minecraft.client.Minecraft client) {
			return 120;
		}

		@Override
		public int getHeight(net.minecraft.client.Minecraft client) {
			return 14;
		}

		@Override
		public void render(
			net.minecraft.client.gui.GuiGraphics graphics,
			net.minecraft.client.Minecraft client,
			amdev.bh.hud.HudRenderContext context,
			amdev.bh.config.BetterHudsConfig.WidgetConfig widgetConfig,
			int x,
			int y
		) {
			graphics.drawString(client.font, "Example", x, y, widgetConfig.textColor, false);
		}
	}
}
```

## HudWidget Contract

Implement `amdev.bh.hud.HudWidget`.

### `id()`

- Must be unique and stable.
- Used as the persistence key in profiles/config.
- Changing an existing ID is a breaking migration for users (they lose stored settings/position for that widget).

### `displayName()`

- Name shown in Better Huds UI.
- Recommended: translatable component, not hardcoded literal.

### `shouldRender(...)`

Overloads:

- `shouldRender(Minecraft client)`
- `shouldRender(Minecraft client, BetterHudsConfig config)`
- `shouldRender(Minecraft client, BetterHudsConfig config, HudRenderContext context)`

Recommendation:

- Override only the most specific overload (`client, config, context`) unless you need simpler defaults.
- Return `false` when your widget has nothing useful to display.
- In editor mode, Better Huds still renders enabled widgets for placement preview.

### `getWidth/getHeight`

- Used for hitbox, placement, snapping, clamping, and backgrounds.
- Return accurate dimensions.
- If widget size changes based on toggles/text/content, override config-aware overloads:
  - `getWidth(Minecraft, BetterHudsConfig, WidgetConfig)`
  - `getHeight(Minecraft, BetterHudsConfig, WidgetConfig)`

### `render(...)`

- Coordinates `x`/`y` are local widget-space origin after layout transform.
- Draw only your widget content.
- Better Huds handles global scale and optional card/background frame.

## HudRenderContext

`HudRenderContext` gives frame-level context to `render(...)`:

- `config()`: full Better Huds config (active profile, global options)
- `metrics()`: runtime metric tracker (for built-in metric-driven widgets)
- `itemHistory()`: tracked inventory change events
- `editorMode()`: true when rendering in HUD editor/preview
- `miniInventoryVisible()`: current mini inventory keybind visibility state

## WidgetConfig Usage

Each widget gets a per-profile `WidgetConfig`:

- `enabled`
- `x`, `y`, `anchor`
- `scale`
- `background`, `backgroundColor`
- `showText`, `textColor`
- `toggles` (`Map<String, Boolean>`)
- `values` (`Map<String, Integer>`)

Use helpers:

- `widgetConfig.toggle("key", defaultValue)`
- `widgetConfig.setToggle("key", value)`
- `widgetConfig.intValue("key", defaultValue)`
- `widgetConfig.setIntValue("key", value)`

Example:

```java
boolean compact = widgetConfig.toggle("compact", false);
int rows = widgetConfig.intValue("rows", 3);
```

## Rendering Lifecycle Notes

- Better Huds runs once per HUD frame.
- Your widget is skipped if:
  - global HUD is disabled
  - `enabled == false`
  - not editor mode and `shouldRender(...) == false`
- Layout clamps widgets on-screen and applies configured scale.

## UI and Settings Behavior for External Widgets

External widgets automatically get:

- card in widget settings grid
- enable/disable toggle
- generic appearance settings (text on/off, colors, scale, background, rainbow text)
- drag/resize in editor
- profile persistence/import/export support

Current limitation:

- No dedicated API yet for injecting a custom per-widget settings panel section.
- Only built-in widget IDs have custom behavior controls in the settings screen.

## Best Practices

- Keep `id()` constant forever.
- Avoid per-frame allocations inside `render`.
- Cache expensive values and recompute only when input changes.
- Null-check `client.player`, `client.level`, and server-only values.
- Keep `getWidth/getHeight` and actual drawing dimensions aligned.
- Prefer translatable strings for visible text.

## Troubleshooting

### Widget never appears

- Confirm your entrypoint is in `fabric.mod.json` under `better-huds`.
- Confirm Better Huds is loaded and listed as dependency.
- Ensure widget is enabled in Better Huds settings.
- Verify `shouldRender(...)` is not always returning `false`.

### Widget appears in editor but not in gameplay

- Usually caused by your render condition in `shouldRender(...)`.
- Check if you rely on unavailable runtime state outside worlds/servers.

### Widget position/settings reset

- Usually due to changing widget `id()`.
- Keep the same ID and migrate manually if renaming is required.

## Complete Example with Toggles/Values

```java
@Override
public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
	return widgetConfig.toggle("compact", false) ? 80 : 140;
}

@Override
public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
	int rows = Math.max(1, Math.min(10, widgetConfig.intValue("rows", 3)));
	return 6 + rows * 10;
}

@Override
public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
	int color = widgetConfig.textColor;
	int rows = Math.max(1, Math.min(10, widgetConfig.intValue("rows", 3)));
	for (int i = 0; i < rows; i++) {
		graphics.drawString(client.font, "Row " + (i + 1), x, y + i * 10, color, false);
	}
}
```
