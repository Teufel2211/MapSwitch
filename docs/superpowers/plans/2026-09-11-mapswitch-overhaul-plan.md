# MapSwitch Overhaul — Implementation Plan

**Spec:** docs/superpowers/specs/2026-09-11-mapswitch-overhaul-design.md (3620f87)
**Branch:** development (C:\Users\Steven\Desktop\Mods\MapSwitch)
**Constraints:** Fabric 1.21.11, Java 21, preserve maps/<map>/, config/mapswitch.json, commands /map switch + /mapswitch

## Phasen

### Phase 1 — Domain + Ports (Core, keine Minecraft-Imports)
- com.mapswitch.domain.MapId — normalized identifier (lowercase, regex ^[a-z0-9_\-]+$)
- com.mapswitch.domain.MapDefinition — id, ootPath, dimensions
- com.mapswitch.domain.PlayerData — wrapper für NBT/stats/adv Fallback-Logik
- com.mapswitch.port.MapRepository — list(), ind(MapId), exists(MapId)
- com.mapswitch.port.PlayerDataPort — save(UUID, MapId), load(UUID, MapId): Result
- com.mapswitch.port.WorldRegistryPort — ensureDimensions(MapId): Result
- com.mapswitch.port.TeleportPort — 	eleport(Player, Dimension, Pos): Result
- com.mapswitch.port.TabListPort, ConfigPort, HintPort
- Tests: Domain unit tests (JUnit5, TempDir)

### Phase 2 — Adapter (Fabric/Minecraft)
- dapter.FileMapRepository — scannt ./maps/ nach playerdata/, egion/ etc.
- dapter.NbtPlayerDataAdapter — liest/schreibt playerdata/<uuid>.dat, stats/*.json, dvancements/*.json
- dapter.DataPackWorldRegistryAdapter — erzeugt datapacks/mapswitch-generated, import DIM-1/, DIM1/
- dapter.ChunkTeleportAdapter — force chunk, teleport (0.5,100,0.5), invuln 2s
- dapter.FileConfigAdapter — config/mapswitch.json
- Jeder Adapter <150 Zeilen, nur via Port testbar

### Phase 3 — Services (Orchestrierung)
- service.MapService.switch(player, targetMapId): Result<Void, MapSwitchException>
  1. validate via MapRepository
  2. PlayerDataPort.save(current)
  3. PlayerDataPort.load(target) || reset
  4. WorldRegistryPort.ensureDimensions
  5. TeleportPort.teleport
  6. TabListPort.syncAll + HintPort.show
- service.TabListService, service.HintService
- MapContext — currentMapId per player (memory)
- Typed Exceptions: MapNotFoundException, PlayerDataException(path+cause), TeleportFailedException, RegistryFailureException

### Phase 4 — Commands (Dünne Adapter)
- command.MapSwitchCommand refactor: parsen ? MapService.switch ? player feedback
- Behalte /map switch + /mapswitch, Tab-Completion via MapRepository.list()
- Neue Commands /mapswitch list + /mapswitch debug (später)

### Phase 5 — Tests
- MapServiceTest — mock Ports, cover save?load?teleport?sync, missing file fallback, error paths
- PlayerDataAdapterTest — TempDir maps/<map>/
- TeleportAdapterTest — mock Server

### Phase 6 — Migration & Cleanup
- Alte Klassen migrieren: MapManager ? Ports/Services, PlayerDataManager ? Adapter, TeleportUtils ? Adapter, WorldRegistryUtil/MapsBootstrapManager ? Adapter
- MapSwitchMod.java entrypoint auf Services umstellen
- Alte world/exception ? domain/exception Result-Typ
- Build verify: ./gradlew build -x test + ./gradlew test

## Reihenfolge
1?2?3?4?5?6, jede Phase commit + push auf development, keine Edits auf main.

## Erfolgskriterien
- Core ohne Minecraft-Imports, 100% unit-testbar
- Kein God Object >200 Zeilen
- ./gradlew build -x test grün, bestehende maps/ + config funktionieren unverändert
