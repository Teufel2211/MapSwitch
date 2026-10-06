package com.mapswitch.player;

import com.mapswitch.MapSwitchMod;
import com.mapswitch.map.MapContext;
import com.mapswitch.world.exception.PlayerDataException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class PlayerDataManager {
    private final MinecraftServer server;
    private final Path mapsRoot;

    public PlayerDataManager(MinecraftServer server, Path mapsRoot) {
        this.server = server;
        this.mapsRoot = mapsRoot;
    }

    public void savePlayerForMap(ServerPlayer player, MapContext map) {
        UUID uuid = player.getUUID();
        Path playerFile = map.mapDirectory().resolve("playerdata").resolve(uuid + ".dat");

        try {
            Files.createDirectories(playerFile.getParent());
            CompoundTag nbt = writePlayerNbt(player);
            try (OutputStream out = Files.newOutputStream(playerFile)) {
                NbtIo.writeCompressed(nbt, out);
            }
            saveStatsAndAdvancements(player, map);
        } catch (Exception ex) {
            throw new PlayerDataException("Failed to save player data for map " + map.name(), ex);
        }
    }

    public void loadPlayerForMap(ServerPlayer player, MapContext map) {
        UUID uuid = player.getUUID();
        Path playerFile = map.mapDirectory().resolve("playerdata").resolve(uuid + ".dat");

        if (Files.notExists(playerFile)) {
            player.getInventory().clearContent();
            player.getEnderChestInventory().clearContent();
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
            player.setExperienceLevels(0);
            player.setExperiencePoints(0);
            resetExperienceProgress(player);
            return;
        }

        try {
            CompoundTag nbt;
            try (InputStream in = Files.newInputStream(playerFile)) {
                nbt = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
            }
            readPlayerNbt(player, nbt);
            loadStatsAndAdvancements(player, map);
        } catch (Exception ex) {
            MapSwitchMod.LOGGER.error(
                    "[MapSwitch] Failed to load player data file '{}' for map '{}'",
                    playerFile,
                    map.name(),
                    ex
            );
            throw new PlayerDataException(
                    "Failed loading player data for map " + map.name() + ": " + ex.getClass().getSimpleName() +
                            (ex.getMessage() == null ? "" : " - " + ex.getMessage()),
                    ex
            );
        }
    }

    private void saveStatsAndAdvancements(ServerPlayer player, MapContext map) throws IOException {
        Path statsSource = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_STATS_DIR).resolve(player.getStringUUID() + ".json");
        Path advSource = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_ADVANCEMENTS_DIR).resolve(player.getStringUUID() + ".json");

        Path statsTarget = map.mapDirectory().resolve("stats").resolve(player.getStringUUID() + ".json");
        Path advTarget = map.mapDirectory().resolve("advancements").resolve(player.getStringUUID() + ".json");

        if (Files.exists(statsSource)) {
            Files.createDirectories(statsTarget.getParent());
            Files.copy(statsSource, statsTarget, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        if (Files.exists(advSource)) {
            Files.createDirectories(advTarget.getParent());
            Files.copy(advSource, advTarget, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void loadStatsAndAdvancements(ServerPlayer player, MapContext map) throws IOException {
        Path statsSource = map.mapDirectory().resolve("stats").resolve(player.getStringUUID() + ".json");
        Path advSource = map.mapDirectory().resolve("advancements").resolve(player.getStringUUID() + ".json");

        Path statsTarget = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_STATS_DIR).resolve(player.getStringUUID() + ".json");
        Path advTarget = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_ADVANCEMENTS_DIR).resolve(player.getStringUUID() + ".json");
        Files.createDirectories(statsTarget.getParent());
        Files.createDirectories(advTarget.getParent());

        if (Files.exists(statsSource)) {
            Files.copy(statsSource, statsTarget, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.deleteIfExists(statsTarget);
        }
        if (Files.exists(advSource)) {
            Files.copy(advSource, advTarget, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.deleteIfExists(advTarget);
        }

        refreshTrackers(player);
    }

    private void refreshTrackers(ServerPlayer player) {
        try {
            Object playerManager = server.getPlayerList();
            Method createStatHandler = playerManager.getClass().getMethod("createStatHandler", ServerPlayer.class);
            Object statHandler = createStatHandler.invoke(playerManager, player);
            Method setStatHandler = findMethod(player.getClass(), "setStatHandler", 1);
            setStatHandler.invoke(player, statHandler);
        } catch (Throwable t) {
            MapSwitchMod.LOGGER.warn("[MapSwitch] Could not hot-reload stats handler for {}", player.getName().getString(), t);
        }

        try {
            Method reloadAdvancements = findMethod(player.getClass(), "onSpawn", 0);
            reloadAdvancements.invoke(player);
        } catch (Throwable t) {
            MapSwitchMod.LOGGER.warn("[MapSwitch] Could not hot-reload advancements for {}", player.getName().getString(), t);
        }
    }

    private CompoundTag writePlayerNbt(ServerPlayer player) throws Exception {
        // 26.x Value-based player serialization API (unobfuscated Mojang names).
        try {
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, server.registryAccess());
            player.saveWithoutId(output);
            return output.buildResult();
        } catch (Throwable ignored) {
        }

        CompoundTag nbt = new CompoundTag();

        // Mapping names changed across releases; resolve the first compatible writer.
        for (Method method : player.getClass().getMethods()) {
            if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == CompoundTag.class) {
                if (!method.getName().equals("saveWithoutId") && !method.getName().equals("save")
                        && !method.getName().equals("addAdditionalSaveData")) {
                    continue;
                }
                Object result = method.invoke(player, nbt);
                if (result instanceof CompoundTag out) {
                    return out;
                }
                return nbt;
            }
        }

        Method method = findMethod(player.getClass(), "addAdditionalSaveData", 1);
        if (method.getParameterTypes()[0] == CompoundTag.class) {
            method.invoke(player, nbt);
            return nbt;
        }
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, server.registryAccess());
        method.invoke(player, output);
        return output.buildResult();
    }

    private void readPlayerNbt(ServerPlayer player, CompoundTag nbt) throws Exception {
        // 26.x Value-based player serialization API (unobfuscated Mojang names).
        try {
            player.load(TagValueInput.create(ProblemReporter.DISCARDING, server.registryAccess(), nbt));
            return;
        } catch (Throwable ignored) {
        }

        for (String methodName : new String[]{"load", "readAdditionalSaveData", "readData", "readCustomData"}) {
            for (Method method : player.getClass().getMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                    continue;
                }
                if (method.getParameterTypes()[0] == CompoundTag.class) {
                    method.invoke(player, nbt);
                    return;
                }
            }
        }
        throw new NoSuchMethodException("No compatible player NBT read method found");
    }

    private void resetExperienceProgress(ServerPlayer player) {
        try {
            Method method = player.getClass().getMethod("setExperienceProgress", float.class);
            method.invoke(player, 0.0f);
        } catch (Throwable ignored) {
        }
    }

    private Method findMethod(Class<?> type, String methodName, int parameterCount) throws NoSuchMethodException {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == parameterCount) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new NoSuchMethodException(type.getName() + "#" + methodName + "/" + parameterCount);
    }

    public Path getMapsRoot() {
        return mapsRoot;
    }
}


