# Developer Notes

## Release Workflow
1. Version in `gradle.properties` erhoehen (`mod_version`).
2. Build: `.\gradlew.bat clean build -x test`
3. Changelog aktualisieren (`CHANGELOG.md`).
4. Upload auf Modrinth + CurseForge.

## Auto-Changelog Script
- Script: `scripts/release/generate-release-notes.ps1`
- Beispiel:
  - `.\scripts\release\generate-release-notes.ps1 -Version 1.0.2 -SinceTag v1.0.1`

## Wiki auf GitHub publizieren
GitHub-Wiki ist ein separates Git-Repo (`<repo>.wiki.git`).
Kopiere die Dateien aus `docs/wiki/` in dieses Wiki-Repo.
