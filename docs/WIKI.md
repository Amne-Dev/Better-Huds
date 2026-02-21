# Better Huds Wiki

This page is the user-focused guide for Better Huds.

## Requirements

- Minecraft `>=1.21`
- Fabric Loader `0.18.0+`
- Fabric API

## Core Features

- Drag-and-drop HUD editor with snapping and resize handles
- Widget settings grid with per-widget toggles and style controls
- Profile system with import/export and per-profile layouts
- Main menu shortcut button to open the HUD position editor
- Keybind tab for Better Huds actions

## Built-In Widgets

- Armor HUD
- Held item / hand HUD
- Survival status HUD
- Keystrokes HUD
- Sprint status HUD
- FPS HUD
- Ping HUD
- Coordinates HUD
- Speed HUD
- Clock HUD
- Biome HUD
- Direction/compass HUD
- Crosshair HUD (custom pattern support)
- Consumables HUD
- Item history HUD
- Item counter HUD
- Status effects HUD
- Mini inventory HUD

## Keybinds (Default)

- Open HUD editor: `Right Shift`
- Toggle HUD: `H`
- Item counter setup: `O`
- Mini inventory toggle/hold: `V`

Keybinds can be changed in Minecraft controls, and the Better Huds keybinds tab points users there.

## Profiles

- Create multiple HUD profiles
- Rename/delete/export from profile context menu
- Import by pasting profile text into the import screen
- Profile data includes widget placement, visibility, and per-widget settings

## Crosshair Notes

- Better Huds can replace the vanilla crosshair
- Crosshair editor supports a draw grid (pen/eraser/clear)
- Optional invert-style rendering is available in widget settings

## Build/Dev Notes

- Run one version profile: `.\run-version.bat`
- Build all configured versions: `.\build-all-versions.bat`
- Build matrix file: `scripts/build-profiles.json`
- Multi-version artifacts output: `build/multi-version/<minecraft-version>/`
- Use `compat_group` in `scripts/build-profiles.json` to group close versions (example: `1.21.9-1.21.10`)
- Default build-all behavior is every profile/version (use `-BuildByCompatGroup` for one build per `compat_group`)

## API for Other Mod Authors

Better Huds supports external widget registration through Fabric entrypoints.

- Start here: `docs/API.md`
- Main API class: `amdev.bh.api.BetterHudsApi`
