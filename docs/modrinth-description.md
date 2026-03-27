# MapSwitch

MapSwitch is a Fabric server mod for multi-world gameplay with strict per-map player-data separation.

## What It Does
- Switch players between maps with:
  - `/mapswitch <map>`
  - `/map switch <map>`
- Keeps map-specific player data isolated:
  - inventory
  - XP / health / hunger
  - ender chest
  - stats
  - advancements
- Supports map-based tab-list separation.
- Shows active map hints in actionbar.

## Folder Layout
- Maps are read from the server root:
  - `./maps/<map>/`

Expected per-map folders:
- `playerdata/`
- `stats/`
- `advancements/`
- `DIM-1/` (nether source)
- `DIM1/` (end source)
- optional overworld `region/`, `entities/`, `poi/`

## Notes
- Built for Fabric + Minecraft 1.21.11.
- Server-side focused mod.
- Requires Fabric API.
