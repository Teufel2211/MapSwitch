package com.mapswitch.player;

import com.mapswitch.MapSwitchMod;
import com.mapswitch.map.MapContext;
import com.mapswitch.world.exception.PlayerDataException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;

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

    public void savePlayerForMap(ServerPlayerEntity player, MapContext map) {
        UUID uuid = player.getUuid();
        Path playerFile = map.mapDirectory().resolve("playerdata").resolve(uuid + ".dat");

        try {
            Files.createDirectories(playerFile.getParent());
            NbtCompound nbt = writePlayerNbt(player);
            try (OutputStream out = Files.newOutputStream(playerFile)) {
                NbtIo.writeCompressed(nbt, out);
            }
            saveStatsAndAdvancements(player, map);
        } catch (Exception ex) {
            throw new PlayerDataException("Failed to save player data for map " + map.name(), ex);
        }
    }

    public void loadPlayerForMap(ServerPlayerEntity player, MapContext map) {
        UUID uuid = player.getUuid();
        Path playerFile = map.mapDirectory().resolve("playerdata").resolve(uuid + ".dat");

        if (Files.notExists(playerFile)) {
            player.getInventory().clear();
            player.getEnderChestInventory().clear();
            player.setHealth(player.getMaxHealth());
            player.getHungerManager().setFoodLevel(20);
            player.setExperienceLevel(0);
            player.setExperiencePoints(0);
            resetExperienceProgress(player);
            return;
        }

        try {
            NbtCompound nbt;
            try (InputStream in = Files.newInputStream(playerFile)) {
                nbt = NbtIo.readCompressed(in, NbtSizeTracker.ofUnlimitedBytes());
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

    private void saveStatsAndAdvancements(ServerPlayerEntity player, MapContext map) throws IOException {
        Path statsSource = server.getSavePath(net.minecraft.util.WorldSavePath.STATS).resolve(player.getUuidAsString() + ".json");
        Path advSource = server.getSavePath(net.minecraft.util.WorldSavePath.ADVANCEMENTS).resolve(player.getUuidAsString() + ".json");

        Path statsTarget = map.mapDirectory().resolve("stats").resolve(player.getUuidAsString() + ".json");
        Path advTarget = map.mapDirectory().resolve("advancements").resolve(player.getUuidAsString() + ".json");

        if (Files.exists(statsSource)) {
            Files.createDirectories(statsTarget.getParent());
            Files.copy(statsSource, statsTarget, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        if (Files.exists(advSource)) {
            Files.createDirectories(advTarget.getParent());
            Files.copy(advSource, advTarget, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void loadStatsAndAdvancements(ServerPlayerEntity player, MapContext map) throws IOException {
        Path statsSource = map.mapDirectory().resolve("stats").resolve(player.getUuidAsString() + ".json");
        Path advSource = map.mapDirectory().resolve("advancements").resolve(player.getUuidAsString() + ".json");

        Path statsTarget = server.getSavePath(net.minecraft.util.WorldSavePath.STATS).resolve(player.getUuidAsString() + ".json");
        Path advTarget = server.getSavePath(net.minecraft.util.WorldSavePath.ADVANCEMENTS).resolve(player.getUuidAsString() + ".json");
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

    private void refreshTrackers(ServerPlayerEntity player) {
        try {
            Object playerManager = server.getPlayerManager();
            Method createStatHandler = playerManager.getClass().getMethod("createStatHandler", ServerPlayerEntity.class);
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

    private NbtCompound writePlayerNbt(ServerPlayerEntity player) throws Exception {
        // 1.21+ codec-based player serialization API.
        try {
            NbtWriteView writeView = NbtWriteView.create(ErrorReporter.EMPTY, server.getRegistryManager());
            player.writeData(writeView);
            return writeView.getNbt();
        } catch (Throwable ignored) {
        }

        NbtCompound nbt = new NbtCompound();

        // Mapping names changed across releases; resolve the first compatible writer.
        for (Method method : player.getClass().getMethods()) {
            if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == NbtCompound.class) {
                if (!method.getName().equals("writeNbt") && !method.getName().equals("writeData")) {
                    continue;
                }
                Object result = method.invoke(player, nbt);
                if (result instanceof NbtCompound out) {
                    return out;
                }
                return nbt;
            }
        }

        Method method = findMethod(player.getClass(), "writeCustomDataToNbt", 1);
        method.invoke(player, nbt);
        return nbt;
    }

    private void readPlayerNbt(ServerPlayerEntity player, NbtCompound nbt) throws Exception {
        // 1.21+ codec-based player serialization API.
        try {
            ReadView readView = NbtReadView.create(ErrorReporter.EMPTY, server.getRegistryManager(), nbt);
            player.readData(readView);
            return;
        } catch (Throwable ignored) {
        }

        for (String methodName : new String[]{"readNbt", "readData", "readCustomDataFromNbt", "readCustomData"}) {
            try {
                Method method = player.getClass().getMethod(methodName, NbtCompound.class);
                method.invoke(player, nbt);
                return;
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException("No compatible player NBT read method found");
    }

    private void resetExperienceProgress(ServerPlayerEntity player) {
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
