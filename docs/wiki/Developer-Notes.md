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

## Auto-Roadmap Script
- Script: `scripts/release/update-roadmap-template.ps1`
- Zweck: Aktualisiert `docs/github-project-template.md` automatisch aus den `(pending)` Versionen in `CHANGELOG.md`.
- Beispiel:
  - `.\scripts\release\update-roadmap-template.ps1`

## Auto-Issue Sync (GitHub API)
- Script: `scripts/release/sync-roadmap-issues.ps1`
- Zweck: Erstellt/aktualisiert Milestone-Issues (z. B. `Milestone 1.0.2`) direkt aus `CHANGELOG.md`.
- Voraussetzung:
  - GitHub Token in der Shell setzen:
  - `$env:GITHUB_TOKEN = "YOUR_TOKEN"`
- Beispiel:
  - `.\scripts\release\sync-roadmap-issues.ps1 -Repo "Teufel2211/MapSwitch" -Versions 1.0.2,1.0.3,1.1.0`
- Dry-run:
  - `.\scripts\release\sync-roadmap-issues.ps1 -DryRun`

## GitHub Projects aus Template pflegen
1. `docs/github-project-template.md` aktualisieren (Script oben).
2. In GitHub Project passende Milestones/Issues fuer die dort gelisteten Versionen anlegen.
3. Bei Release: `(pending)` in `CHANGELOG.md` ersetzen, Script neu ausfuehren, Board anpassen.

## Wiki auf GitHub publizieren
GitHub-Wiki ist ein separates Git-Repo (`<repo>.wiki.git`).
Kopiere die Dateien aus `docs/wiki/` in dieses Wiki-Repo.
