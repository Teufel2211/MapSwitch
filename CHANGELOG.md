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
- (pending)
### Improvements
- (pending)
### Fixes
- (pending)
### Internal
- (pending)

## 1.0.3
### Features
- (pending)
### Improvements
- (pending)
### Fixes
- (pending)
### Internal
- (pending)

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
- (pending)
### Improvements
- (pending)
### Fixes
- (pending)
### Internal
- (pending)

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
