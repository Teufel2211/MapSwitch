package com.mapswitch;

import com.mapswitch.command.MapSwitchCommand;
import com.mapswitch.config.ConfigManager;
import com.mapswitch.map.MapManager;
import com.mapswitch.player.PlayerDataManager;
import com.mapswitch.teleport.TeleportUtils;
import com.mapswitch.world.MapsBootstrapManager;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class MapSwitchMod implements ModInitializer, DedicatedServerModInitializer {
    public static final String MOD_ID = "mapswitch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static volatile ConfigManager configManager;
    private static volatile MapManager mapManager;
    private static volatile PlayerDataManager playerDataManager;
    private static volatile TeleportUtils teleportUtils;

    @Override
    public void onInitialize() {
        MapSwitchCommand.register();
        registerLifecycleHooks();
    }

    @Override
    public void onInitializeServer() {
        LOGGER.info("[MapSwitch] Server entrypoint loaded.");
    }

    private static void registerLifecycleHooks() {
        ServerLifecycleEvents.SERVER_STARTING.register(MapSwitchMod::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(MapSwitchMod::onServerStopping);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            TeleportUtils util = teleportUtils;
            ConfigManager config = configManager;
            if (util != null && config != null) {
                util.ensurePlayerMapHint(handler.player, config.getConfig().default_map);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            TeleportUtils util = teleportUtils;
            if (util != null) {
                util.tick(server);
            }
        });
    }

    private static void onServerStarting(MinecraftServer server) {
        Path runDir = server.getRunDirectory();
        configManager = new ConfigManager(runDir.resolve("config").resolve("mapswitch.json"));
        configManager.loadOrCreate();

        Path mapsRoot = runDir.resolve("maps");
        MapsBootstrapManager.BootstrapResult bootstrap = new MapsBootstrapManager()
                .ensureBootstrap(server, mapsRoot, configManager.getConfig().allowed_maps);
        if (bootstrap.changed()) {
            LOGGER.warn(
                    "[MapSwitch] Generated/updated datapack at {} and imported map data into {}. " +
                            "Restart the server once so new dimensions are loaded.",
                    bootstrap.datapackPath().toAbsolutePath(),
                    bootstrap.worldRoot().toAbsolutePath()
            );
        }

        mapManager = new MapManager(server, mapsRoot, configManager.getConfig());
        mapManager.scanAndPrepareMaps();

        playerDataManager = new PlayerDataManager(server, mapsRoot);
        teleportUtils = new TeleportUtils(mapManager, playerDataManager);

        LOGGER.info("[MapSwitch] Maps root: {}", runDir.resolve("maps").toAbsolutePath());
        LOGGER.info("[MapSwitch] Loaded maps: {}", mapManager.getLoadedMapNames());
    }

    private static void onServerStopping(MinecraftServer server) {
        TeleportUtils util = teleportUtils;
        if (util != null) {
            util.clear();
        }
    }

    public static ConfigManager configManager() {
        return configManager;
    }

    public static MapManager mapManager() {
        return mapManager;
    }

    public static PlayerDataManager playerDataManager() {
        return playerDataManager;
    }

    public static TeleportUtils teleportUtils() {
        return teleportUtils;
    }
}
