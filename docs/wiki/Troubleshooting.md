# Troubleshooting

## Command erscheint nicht
- Sicherstellen, dass der Server mit der neuen Jar gestartet wurde.
- `logs/latest.log` auf `[MapSwitch]` pruefen.

## "Failed loading player data for map"
- Pruefe, ob `maps/<map>/playerdata/<uuid>.dat` lesbar ist.
- Log-Eintrag mit Root-Cause beachten.

## Map-Dimension nicht geladen
- Nach erstem Start wird Datapack erzeugt.
- Server danach einmal neu starten.

## Tab-Liste nicht getrennt
- Mod-Version pruefen.
- Nach `/mapswitch <map>` kurz warten (periodischer Sync + direkter Sync bei Wechsel).
