# Changelog

## 1.0.0
- Initial public release.
- Commands:
  - `/mapswitch <map>`
  - `/map switch <map>`
- Per-map player-data separation:
  - NBT in `maps/<map>/playerdata/<uuid>.dat`
  - stats in `maps/<map>/stats/<uuid>.json`
  - advancements in `maps/<map>/advancements/<uuid>.json`
- Safe switch flow:
  - save current map data
  - load target map data
  - force target chunk
  - teleport to `(0,100,0)`
  - short invulnerability window
- Automatic datapack bootstrap for per-map dimensions.
- Map-name normalization for valid dimension identifiers.
- Tab-list separation per map.
- Actionbar hint showing active map.
