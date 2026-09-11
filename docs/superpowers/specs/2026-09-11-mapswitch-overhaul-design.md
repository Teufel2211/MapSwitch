# MapSwitch Complete Overhaul — Design (Clean Core + Adapter)

**Date:** 2026-09-11
**Repo:** Teufel2211/MapSwitch (C:\Users\Steven\Desktop\Mods\MapSwitch)
**Branch:** development
**Constraints:** Fabric 1.21.11, Java 21, preserve maps/<map>/playerdata/<uuid>.dat, stats/, dvancements/, DIM-1/, DIM1/, config/mapswitch.json, commands /map switch + /mapswitch

## 1. Architecture Overview

Clean Core + Adapter (Hexagonal) isolating domain from Minecraft API.

- **Core Domain** (com.mapswitch.domain): MapId, MapDefinition, PlayerData — no 
et.minecraft imports.
- **Ports** (com.mapswitch.port): interfaces MapRepository, PlayerDataPort, WorldRegistryPort, TeleportPort, TabListPort, ConfigPort.
- **Adapters** (com.mapswitch.adapter): Fabric/Minecraft implementations FileMapRepository, NbtPlayerDataAdapter, DataPackWorldRegistryAdapter, ChunkTeleportAdapter, etc.
- **Services** (com.mapswitch.service): MapService orchestrates switch, TabListService, HintService.
- **Commands** (com.mapswitch.command): thin adapters parsing args ? calling MapService.

Benefits: Core 100% unit-testable, Minecraft API mocked via ports, God Objects eliminated.

## 2. Components & Responsibilities

Each class <200 lines, 1 interface + 1 impl:

- **Domain:** MapId (normalized identifier), MapDefinition (folders, dimensions), PlayerData (NBT wrapper).
- **MapRepository:** list(), ind(MapId), exists(MapId) — scans ./maps/ for required subfolders.
- **PlayerDataPort:** save(player, mapId), load(player, mapId) — handles playerdata/*.dat, stats/*.json, dvancements/*.json with PlayerDataException on failure.
- **WorldRegistryPort:** ensureDimensions(mapId) — generates <world>/datapacks/mapswitch-generated with <map>_overworld/_nether/_end, import DIM-1/, DIM1/.
- **TeleportPort:** 	eleport(player, dimension, pos) — force chunk load, teleport (0.5,100,0.5), 2s invulnerability, throws TeleportFailedException.
- **MapService:** switch(player, targetMapId): Result<Void, MapSwitchException> — sequence: validate ? saveCurrent ? loadTarget (fallback reset) ? ensureDimensions ? teleport ? tabSync ? hint.
- **TabListPort/TabListService:** sync(player), syncAll() — filter by MapContext.
- **ConfigPort:** loads config/mapswitch.json (default_map, llowed_maps, per-map teleport targets future).
- **MapContext:** holds currentMapId per player (in-memory + persist).

## 3. Data Flow & Error Handling

**Flow:**
`
Command (/map switch <map>) 
  -> MapService.switch(player, targetMapId)
    1. MapRepository.find(target) or MapNotFoundException
    2. PlayerDataPort.save(player, currentMapId)
    3. PlayerDataPort.load(player, targetMapId) or reset if missing
    4. WorldRegistryPort.ensureDimensions(target) or RegistryFailureException
    5. TeleportPort.teleport(player, dim, pos) or TeleportFailedException
    6. TabListService.syncAll()
    7. HintService.show(player, targetMapId)
`

**Errors:** Typed MapSwitchException hierarchy (MapNotFoundException, PlayerDataException with file path + cause, RegistryFailureException, TeleportFailedException). Services return Result<T,E> internally, commands map to player messages + LOGGER.warn with path/cause. No server crash on dynamic world API missing — log warning + fallback. Missing target Dimension ? switch fails gracefully, server stays up.

## 4. Testability & Project Structure

**Structure:**
`
src/main/java/com/mapswitch/
  domain/ (MapId, MapDefinition, PlayerData)
  port/ (interfaces)
  adapter/ (FileMapRepository, NbtPlayerDataAdapter, DataPackWorldRegistryAdapter, ChunkTeleportAdapter, FileConfigAdapter)
  service/ (MapService, TabListService, HintService)
  command/ (MapSwitchCommand)
  world/ (deprecated ? migrated to adapter/service)
src/test/java/com/mapswitch/
  service/MapServiceTest (mock ports)
  adapter/PlayerDataAdapterTest (TempDir maps/<map>/)
  adapter/TeleportAdapterTest
`

**Testing:** JUnit5 useJUnitPlatform(), core tests without Minecraft, adapters mocked. PlayerDataAdapterTest uses @TempDir for file IO. No integration against real world yet — future.

**Build:** Keep minecraft_version=1.21.11, yarn 1.21.11+build.4, loader 0.18.5, loom 1.13.3, abric 0.141.3, Java 21. No breaking changes to maps/ layout or config/mapswitch.json.

## 5. Migration Plan (Existing Code)

- MapManager.java (large) ? split into MapRepository + MapService
- PlayerDataManager.java ? PlayerDataPort + PlayerDataDomain
- TeleportUtils.java ? TeleportPort
- WorldRegistryUtil.java + MapsBootstrapManager.java ? WorldRegistryPort
- MapSwitchCommand.java ? thin command delegating to MapService
- MapContext.java retained as domain, ConfigManager.java ? ConfigPort
- Exceptions moved to domain/exception ? reused as typed errors.

## 6. Success Criteria

- All core domain unit tests pass without Minecraft.
- MapService.switch mocked tests cover save?load?teleport?sync, missing file fallback, and error paths.
- No class >200 lines, no God Object.
- Existing commands/config/maps folders work unchanged (compatibility).
- development branch builds with ./gradlew build -x test.

