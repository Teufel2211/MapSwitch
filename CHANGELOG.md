# Changelog

Dieses Changelog ist die zentrale Quelle fuer Modrinth + CurseForge.

## Versionierung (Schema)
- Wir nutzen SemVer: `MAJOR.MINOR.PATCH`
- Beispiel:
  - `1.0.0` erste stabile Release
  - `1.0.1` bis `1.0.9` fuer kleine Fixes/kleine Verbesserungen
  - danach `1.1.0` fuer neues Feature-Buendel
  - danach wieder `1.1.1` bis `1.1.9`, dann `1.2.0`, usw.
- Regeln:
  - `PATCH` (`x.x.+1`): Bugfixes, kleine Anpassungen, keine Breaking Changes
  - `MINOR` (`x.+1.0`): neue Features, rueckwaertskompatibel
  - `MAJOR` (`+1.0.0`): Breaking Changes

## 1.0.0
### Features
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

## 1.0.1
### Improvements
- Marketplace-Metadaten vervollstaendigt in `fabric.mod.json`:
  - `homepage`, `sources`, `issues`
  - Icon-Eintrag aktiv (`assets/mapswitch/icon.png`)
- Release-Dokumentation fuer Modrinth und CurseForge hinzugefuegt:
  - `PUBLISHING.md`
  - `docs/modrinth-description.md`
  - `docs/curseforge-description.md`
  - `docs/changelog-modrinth.md`
  - `docs/changelog-curseforge.md`
- Lizenz und Changelog-Struktur fuer Plattform-Uploads vereinheitlicht:
  - `LICENSE`
  - `CHANGELOG.md` als zentrale Quelle

### Internal
- Build-/Release-Artefakte und Wrapper-Setup fuer reproduzierbare Uploads verifiziert.

## 1.0.2
### Features
- Add diagnostics command `/mapswitch debug`.
- Add map overview command `/mapswitch list`.
### Improvements
- Improve operator feedback for map switch errors and map availability hints.
- Expand wiki troubleshooting for common setup/runtime issues.
### Fixes
- Improve player-data loading fallback path with clearer root-cause reporting.
- Harden file/path handling for per-map playerdata/stat/advancement loading.
### Internal
- Align roadmap + issue planning for `1.0.2` release scope.

## 1.0.3
### Features
- Add configurable teleport target per map via config.
- Add automatic restore to last active map on player join.
- Add map aliases (example: `hc -> hardcore`).
### Improvements
- Improve map-switch success/error UX messages for players.
- Improve admin-facing diagnostics output for map resolution.
### Fixes
- Stabilize join/map-hint sync when player reconnects during switch flow.
### Internal
- Prepare migration path for next minor (`1.1.0`) feature bundle.

## 1.0.4
### Features
- (pending)
### Improvements
- (pending)
### Fixes
- (pending)
### Internal
- (pending)

## 1.0.5
### Features
- (pending)
### Improvements
- (pending)
### Fixes
- (pending)
### Internal
- (pending)

## 1.0.6
### Features
- (pending)
### Improvements
- (pending)
### Fixes
- (pending)
### Internal
- (pending)

## 1.0.7
### Features
- (pending)
### Improvements
- (pending)
### Fixes
- (pending)
### Internal
- (pending)

## 1.0.8
### Features
- (pending)
### Improvements
- (pending)
### Fixes
- (pending)
### Internal
- (pending)

## 1.0.9
### Features
- (pending)
### Improvements
- (pending)
### Fixes
- (pending)
### Internal
- (pending)

## 1.1.0
### Features
- Add configurable command access modes per map/global (`public` / `restricted` / `whitelist`).
- Add per-map player limits (quota).
- Add per-map gamerule profile application.
- Add migration command for old map folder layouts (with dry-run).
- Add admin analytics command `/mapswitch stats`.
### Improvements
- Improve governance controls for public/community servers with multiple worlds.
- Improve operational visibility for map activity and load distribution.
### Fixes
- Fix edge-cases around permission transitions during runtime config changes.
### Internal
- Promote roadmap from patch-level fixes to a minor feature release.

---

## Vorlage fuer neue Versionen

Kopiere dieses Template fuer jede neue Version:

```md
## 1.0.X
### Features
- ...

### Improvements
- ...

### Fixes
- ...

### Internal
- ...
```

Beispiele fuer den naechsten Ablauf:
- `1.0.1`, `1.0.2`, ..., `1.0.9`
- dann `1.1.0`
- dann `1.1.1`, ..., `1.1.9`
- dann `1.2.0`
