package com.mapswitch.map;

import com.mapswitch.MapSwitchMod;
import com.mapswitch.config.ConfigManager;
import com.mapswitch.world.WorldRegistryUtil;
import com.mapswitch.world.exception.MapNotFoundException;
import com.mapswitch.world.exception.RegistryFailureException;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MapManager {
    private final MinecraftServer server;
    private final Path mapsRoot;
    private final ConfigManager.Config config;
    private final WorldRegistryUtil worldRegistryUtil;
    private final Map<String, MapContext> maps = new LinkedHashMap<>();

    public MapManager(MinecraftServer server, Path mapsRoot, ConfigManager.Config config) {
        this.server = server;
        this.mapsRoot = mapsRoot;
        this.config = config;
        this.worldRegistryUtil = new WorldRegistryUtil();
    }

    public void scanAndPrepareMaps() {
        try {
            Files.createDirectories(mapsRoot);
            for (String mapName : config.allowed_maps) {
                Path mapDir = mapsRoot.resolve(mapName);
                Files.createDirectories(mapDir.resolve("playerdata"));
                Files.createDirectories(mapDir.resolve("stats"));
                Files.createDirectories(mapDir.resolve("advancements"));

                MapContext context = buildContext(mapName, mapDir);
                String lookupKey = toLookupKey(mapName);
                if (maps.containsKey(lookupKey)) {
                    MapSwitchMod.LOGGER.warn("[MapSwitch] Duplicate normalized map id for '{}', skipping.", mapName);
                    continue;
                }
                maps.put(lookupKey, context);
                boolean available = worldRegistryUtil.ensureMapWorldsRegistered(server, context);
                if (!available) {
                    MapSwitchMod.LOGGER.warn(
                            "[MapSwitch] Map '{}' has no loaded overworld dimension '{}'. " +
                                    "Switching to this map will fail until a world provider/datapack registers it.",
                            context.name(),
                            context.overworldKey().getValue()
                    );
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed scanning maps root: " + mapsRoot, ex);
        }
    }

    private MapContext buildContext(String mapName, Path mapDir) {
        String mapId = toIdentifierPath(mapName);
        if (!mapId.equals(mapName)) {
            MapSwitchMod.LOGGER.info("[MapSwitch] Normalized map key '{}' -> '{}'", mapName, mapId);
        }
        Identifier base = Identifier.of(MapSwitchMod.MOD_ID, mapId);
        RegistryKey<World> overworld = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(base.getNamespace(), mapId + "_overworld"));
        RegistryKey<World> nether = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(base.getNamespace(), mapId + "_nether"));
        RegistryKey<World> end = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(base.getNamespace(), mapId + "_end"));

        return new MapContext(mapName, mapId, mapDir, overworld, nether, end);
    }

    public MapContext getMapOrThrow(String mapName) {
        MapContext context = maps.get(toLookupKey(mapName));
        if (context == null) {
            throw new MapNotFoundException("Map not found: " + mapName);
        }
        return context;
    }

    public Optional<MapContext> getMap(String mapName) {
        return Optional.ofNullable(maps.get(toLookupKey(mapName)));
    }

    public Set<String> getLoadedMapNames() {
        Set<String> names = new LinkedHashSet<>();
        for (MapContext context : maps.values()) {
            names.add(context.name());
        }
        return Collections.unmodifiableSet(names);
    }

    public Collection<MapContext> getMaps() {
        return Collections.unmodifiableCollection(maps.values());
    }

    public ServerWorld resolveTargetWorld(MapContext context) {
        ServerWorld world = server.getWorld(context.overworldKey());
        if (world == null && context.name().equals(config.default_map)) {
            world = server.getOverworld();
        }
        if (world == null) {
            throw new RegistryFailureException("Target overworld dimension is not loaded: " + context.overworldKey().getValue());
        }
        return world;
    }

    private String toLookupKey(String input) {
        return toIdentifierPath(input);
    }

    public static String toIdentifierPath(String input) {
        String lowered = input.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        String sanitized = lowered.replaceAll("[^a-z0-9/._-]", "_");
        while (sanitized.contains("__")) {
            sanitized = sanitized.replace("__", "_");
        }
        sanitized = sanitized.replaceAll("^[_./-]+", "").replaceAll("[_./-]+$", "");
        if (sanitized.isBlank()) {
            return "map";
        }
        return sanitized;
    }
}
