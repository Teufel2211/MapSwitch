# Commands

Automatisch aus `src/main/java/com/mapswitch/command/MapSwitchCommand.java` generiert.

## Verfuegbar
- `/mapswitch <mapname>`
- `/map switch <mapname>`
- `/mapswitch list`
- `/mapswitch debug`

## Verhalten
- Der ausfuehrende Spieler wird in die Ziel-Map gewechselt (`<mapname>` Varianten).
- Spieler-Daten werden map-spezifisch geladen.
- Tab-Liste wird nach Map getrennt.
- Actionbar zeigt aktive Map.
- `list` zeigt Map-Verfuegbarkeit und Ziel-Dimensionen.
- `debug` liefert Diagnosewerte (mapsRoot, defaultMap, unavailableMaps).

## Beispiele
- `/mapswitch normal`
- `/map switch hardcore`
- `/mapswitch list`
- `/mapswitch debug`
