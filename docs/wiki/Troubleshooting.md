# Troubleshooting

## Schnell-Diagnose (1.0.2)
- ` /mapswitch list ` zeigt alle konfigurierten Maps mit Verfuegbarkeitsstatus.
- ` /mapswitch debug ` zeigt technische Basisdaten (mapsRoot, defaultMap, unavailableMaps).
- Bei Switch-Fehlern immer zuerst beide Befehle ausfuehren und den Output sichern.

## Command erscheint nicht
- Sicherstellen, dass der Server mit der neuen Jar gestartet wurde.
- `logs/latest.log` auf `[MapSwitch]` pruefen.
- Mit ` /mapswitch list ` testen, ob der Command-Tree korrekt registriert wurde.

## "Unbekannte Map '<name>'"
- Name gegen ` /mapswitch list ` pruefen.
- In `config/mapswitch.json` kontrollieren, ob die Map in `allowed_maps` eingetragen ist.
- Nach Config-Aenderung den Server neu starten.

## "Map ist aktuell nicht verfuegbar"
- ` /mapswitch list ` zeigt den Status pro Map (`verfuegbar` oder `nicht verfuegbar`).
- Wenn `nicht verfuegbar`: Ziel-Dimension ist nicht geladen.
- Nach erstem Start wird das Datapack erzeugt, danach den Server einmal neu starten.
- Bei weiterem Fehler ` /mapswitch debug ` nutzen und `unavailableMaps` pruefen.

## "Failed loading player data for map"
- Pruefen, ob `maps/<map>/playerdata/<uuid>.dat` lesbar und gueltig ist.
- Pruefen, ob `maps/<map>/stats/<uuid>.json` und `maps/<map>/advancements/<uuid>.json` valide JSON-Dateien sind.
- In `logs/latest.log` die Root-Cause-Zeile aus `[MapSwitch] Failed to load player data ...` auswerten.
- Hinweis: Ab 1.0.2 versucht MapSwitch bei Ladefehlern einen Fallback auf ein frisches Profil.

## Pfad-/Datei-Probleme bei Map-Daten
- Keine Symlink-/Junction-Konstrukte verwenden, die aus `maps/` herauszeigen.
- Map-Ordnernamen nur mit einfachen Zeichen (`a-z`, `0-9`, `_`, `-`) verwenden.
- Bei verdaechtigen Altstrukturen betroffene Map sichern und neu anlegen.

## Tab-Liste nicht getrennt
- Mod-Version pruefen.
- Nach `/mapswitch <map>` kurz warten (periodischer Sync + direkter Sync bei Wechsel).
