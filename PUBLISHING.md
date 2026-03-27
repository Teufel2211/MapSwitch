# Publishing Guide (Modrinth + CurseForge)

This project is ready to publish as a Fabric server mod.

## Build
1. Run:
   - `.\gradlew.bat clean build -x test`
2. Upload this file:
   - `build/libs/mapswitch-1.0.0.jar`
3. Optional source upload:
   - `build/libs/mapswitch-1.0.0-sources.jar`

## Required Project Data
- Name: `MapSwitch`
- Slug/ID: `mapswitch`
- Version: `1.0.0`
- Minecraft: `1.21.11`
- Loader: `Fabric`
- Environment: `Server`
- License: `MIT`

## Modrinth
Create a project with:
- Categories: `fabric`, `server-utility`, `world-management` (or closest available)
- Side: `Server`
- License: `MIT`

Use:
- Description source: `docs/modrinth-description.md`
- Changelog source: `CHANGELOG.md` (optional helper: `docs/changelog-modrinth.md`)
- Primary file: `build/libs/mapswitch-1.0.0.jar`

Mark dependencies:
- Required: `Fabric API`

## CurseForge
Create a Fabric project with:
- Game Versions: `Minecraft 1.21.11`, `Fabric`
- Category: `Server Utility` (or closest)
- License: `MIT`

Use:
- Description source: `docs/curseforge-description.md`
- Changelog source: `CHANGELOG.md` (optional helper: `docs/changelog-curseforge.md`)
- Main file: `build/libs/mapswitch-1.0.0.jar`

Mark dependency:
- Required: `Fabric API`

## Final Checklist
- `fabric.mod.json` version is correct via Gradle expansion.
- `README.md` is up to date.
- `LICENSE` exists.
- `CHANGELOG.md` includes current release notes.
- Jar starts successfully on a clean Fabric test server.

## GitHub Project + Wiki
- Project Board setup template:
  - `docs/github-project-template.md`
- Wiki page sources:
  - `docs/wiki/Home.md`
  - `docs/wiki/Installation.md`
  - `docs/wiki/Commands.md`
  - `docs/wiki/Configuration.md`
  - `docs/wiki/Map-Structure.md`
  - `docs/wiki/Troubleshooting.md`
  - `docs/wiki/Developer-Notes.md`
