# Map-Struktur

MapSwitch nutzt den Server-Hauptordner:
- `maps/<map>/`

## Pro Map
- `playerdata/<uuid>.dat`
- `stats/<uuid>.json`
- `advancements/<uuid>.json`
- `DIM-1/` (Nether source)
- `DIM1/` (End source)
- optional Overworld-Daten:
  - `region/`
  - `entities/`
  - `poi/`

## Hinweis
Map-Namen werden intern in gueltige Identifier normalisiert (z. B. `Hardcore` -> `hardcore`).
