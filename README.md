# MapSwitch (Fabric 1.21.11)

Diese Datei beschreibt gesammelt, was die Mod aktuell kann.

## Quickstart (Admin)
1. `build/libs/mapswitch-1.0.0.jar` in den Server-`mods/`-Ordner kopieren.
2. Maps in den Server-Hauptordner unter `./maps/` legen.
3. Server starten (Datapack/Dimension-Dateien werden vorbereitet).
4. Server neu starten (neue Dimensionen werden geladen).
5. Im Spiel testen mit `/mapswitch <mapname>`.

## Kurzfassung
MapSwitch trennt Spielstände pro Map und erlaubt Spielern, per Command zwischen Maps zu wechseln.

## Commands
- `/map switch <mapname>`
- `/mapswitch <mapname>`
- Beide Commands sind für alle Spieler nutzbar.
- Tab-Completion für verfügbare Maps ist aktiv.

## Map-Quellen und Ordner
- Maps werden aus dem Server-Hauptordner geladen: `./maps/`
- Pro Map werden diese Ordner unterstützt:
  - `playerdata/`
  - `stats/`
  - `advancements/`
  - `DIM-1/` (Nether-Datenquelle)
  - `DIM1/` (End-Datenquelle)
  - Overworld-Daten direkt im Map-Root (`region/`, `entities/`, `poi/` wenn vorhanden)

## Player-Datentrennung pro Map
- Pro Map und Spieler wird gespeichert in:
  - `./maps/<map>/playerdata/<uuid>.dat`
- Beim Wechsel:
  - alte Map-Daten speichern
  - Ziel-Map-Daten laden
  - falls keine Ziel-Datei vorhanden: sauberer Reset auf Basiswerte
- Zusätzliche Trennung:
  - `stats`: `./maps/<map>/stats/<uuid>.json`
  - `advancements`: `./maps/<map>/advancements/<uuid>.json`

## Teleport-Ablauf (sicher)
- Zielchunk wird vorab forciert geladen.
- Teleport auf `(0.5, 100, 0.5)` in die Ziel-Overworld der Map.
- 2 Sekunden Invulnerability nach Wechsel.
- Fehler werden sauber an den Spieler gemeldet.

## Dimension-/Welten-Handling
- Die Mod erzeugt automatisch ein Datapack:
  - `<world>/datapacks/mapswitch-generated`
- Dieses Datapack enthält Dimension-Definitionen für:
  - `<map>_overworld`
  - `<map>_nether`
  - `<map>_end`
- Map-Daten aus `./maps/` werden in passende `dimensions/mapswitch/...` Zielordner importiert.
- Wichtig: Nach erstmaliger Generierung ist ein zusätzlicher Server-Neustart nötig, damit neue Dimensionen geladen sind.

## Map-Normalisierung
- Mapnamen werden für interne IDs normalisiert (Minecraft Identifier kompatibel).
- Beispiel: `Hardcore` -> `hardcore`
- Dadurch keine Crashes mehr durch ungültige Zeichen/Großbuchstaben.

## Tab-Liste (echte Trennung)
- Spieler sehen in der Tab-Liste nur Spieler derselben Map.
- Bei Mapwechsel wird die Tab-Liste sofort neu synchronisiert.
- Zusätzlich periodischer Sync, damit Join/Edge-Cases sauber bleiben.

## Map-Hinweis für Spieler
- Beim Join und nach Mapwechsel bekommt der Spieler Actionbar-Hinweis:
  - `Aktive Map: <map>`

## Config
- Datei: `config/mapswitch.json`
- Felder:
  - `default_map`
  - `allowed_maps`

## Robustheit/Fehlerbilder
- Wenn dynamische Welt-API nicht verfügbar ist, crasht der Server nicht.
- Stattdessen Warnung im Log und Fallback-Verhalten.
- Wenn Ziel-Dimension nicht geladen ist, schlägt nur der Wechsel fehl (kein Startup-Crash).
- PlayerData-Fehler werden mit Dateipfad + Root-Cause geloggt.

## Build/Runtime
- Ziel: Fabric + Minecraft `1.21.11`
- Java 21
- Build mit:
  - `.\gradlew.bat build -x test`
