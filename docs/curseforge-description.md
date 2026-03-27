# MapSwitch (Fabric 1.21.11)

MapSwitch adds map-based world switching with per-map player progression/data split.

## Features
- Commands:
  - `/mapswitch <map>`
  - `/map switch <map>`
- Per-map player-data files in:
  - `maps/<map>/playerdata/<uuid>.dat`
- Per-map stats and advancements split:
  - `maps/<map>/stats/<uuid>.json`
  - `maps/<map>/advancements/<uuid>.json`
- Safe transfer pipeline:
  - save old state
  - load target state
  - force target chunk
  - teleport to target
  - short invulnerability
- Tab-list separation by active map.
- Actionbar map hint for players.

## Installation
1. Install Fabric Loader for Minecraft 1.21.11.
2. Install Fabric API.
3. Put `mapswitch-<version>.jar` in your server `mods` folder.
4. Create your maps under `./maps/` in server root.

## Compatibility
- Server-side utility mod.
- Built for Fabric API on Minecraft 1.21.11.
