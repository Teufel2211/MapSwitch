package com.mapswitch.teleport;

import com.mapswitch.map.MapContext;
import com.mapswitch.map.MapManager;
import com.mapswitch.player.PlayerDataManager;
import com.mapswitch.world.exception.RegistryFailureException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TeleportUtils {
    private static final int INVUL_TICKS = 40;

    private final MapManager mapManager;
    private final PlayerDataManager playerDataManager;
    private final Map<UUID, Integer> invulnerabilityTicks = new HashMap<>();
    private final Map<UUID, String> playerActiveMap = new HashMap<>();
    private final Map<UUID, Set<UUID>> visibleTabEntries = new HashMap<>();
    private int tabSyncTicker;

    public TeleportUtils(MapManager mapManager, PlayerDataManager playerDataManager) {
        this.mapManager = mapManager;
        this.playerDataManager = playerDataManager;
    }

    public void switchPlayerToMap(ServerPlayerEntity player, MapContext targetMap) {
        MapContext oldMap = resolveCurrentMap(player);
        if (oldMap != null) {
            playerDataManager.savePlayerForMap(player, oldMap);
        }

        playerDataManager.loadPlayerForMap(player, targetMap);
        ServerWorld targetWorld = mapManager.resolveTargetWorld(targetMap);
        ensureSpawnChunkLoaded(targetWorld);

        player.teleport(targetWorld, 0.5, 100.0, 0.5, Set.of(), player.getYaw(), player.getPitch(), false);
        playerActiveMap.put(player.getUuid(), targetMap.name());
        updatePlayerIndicators(player, targetMap.name());
        refreshTabVisibility(targetWorld.getServer());
        player.setInvulnerable(true);
        invulnerabilityTicks.put(player.getUuid(), INVUL_TICKS);
    }

    private MapContext resolveCurrentMap(ServerPlayerEntity player) {
        String current = playerActiveMap.get(player.getUuid());
        if (current == null) {
            return null;
        }
        return mapManager.getMap(current).orElse(null);
    }

    private void ensureSpawnChunkLoaded(ServerWorld world) {
        try {
            ChunkPos spawnChunk = new ChunkPos(BlockPos.ORIGIN);
            world.setChunkForced(spawnChunk.x, spawnChunk.z, true);
        } catch (Exception ex) {
            throw new RegistryFailureException("Could not force-load target chunk", ex);
        }
    }

    public void tick(MinecraftServer server) {
        tabSyncTicker++;
        if (tabSyncTicker >= 20) {
            tabSyncTicker = 0;
            refreshTabVisibility(server);
        }

        if (invulnerabilityTicks.isEmpty()) {
            return;
        }
        var iterator = invulnerabilityTicks.entrySet().iterator();
        while (iterator.hasNext()) {
            var next = iterator.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(next.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }
            int left = next.getValue() - 1;
            if (left <= 0) {
                player.setInvulnerable(false);
                iterator.remove();
            } else {
                next.setValue(left);
            }
        }
    }

    public void clear() {
        invulnerabilityTicks.clear();
        playerActiveMap.clear();
        visibleTabEntries.clear();
        tabSyncTicker = 0;
    }

    public void ensurePlayerMapHint(ServerPlayerEntity player, String defaultMap) {
        if (playerActiveMap.containsKey(player.getUuid())) {
            updatePlayerIndicators(player, playerActiveMap.get(player.getUuid()));
            return;
        }
        playerActiveMap.put(player.getUuid(), defaultMap);
        updatePlayerIndicators(player, defaultMap);
    }

    private void refreshTabVisibility(MinecraftServer server) {
        if (server == null) {
            return;
        }
        Collection<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        if (players.isEmpty()) {
            visibleTabEntries.clear();
            return;
        }

        Map<UUID, ServerPlayerEntity> byId = new HashMap<>();
        for (ServerPlayerEntity player : players) {
            byId.put(player.getUuid(), player);
            playerActiveMap.putIfAbsent(player.getUuid(), mapManager.getMaps().stream().findFirst().map(m -> m.name()).orElse("normal"));
        }

        for (ServerPlayerEntity viewer : players) {
            String viewerMap = playerActiveMap.getOrDefault(viewer.getUuid(), "normal");
            Set<UUID> desired = new HashSet<>();
            for (ServerPlayerEntity other : players) {
                String otherMap = playerActiveMap.getOrDefault(other.getUuid(), "normal");
                if (viewerMap.equalsIgnoreCase(otherMap)) {
                    desired.add(other.getUuid());
                }
            }

            Set<UUID> previous = visibleTabEntries.getOrDefault(viewer.getUuid(), Set.of());
            List<UUID> toRemove = new ArrayList<>();
            for (UUID id : previous) {
                if (!desired.contains(id)) {
                    toRemove.add(id);
                }
            }

            if (!toRemove.isEmpty()) {
                viewer.networkHandler.sendPacket(new PlayerRemoveS2CPacket(toRemove));
            }

            List<ServerPlayerEntity> toAdd = new ArrayList<>();
            for (UUID id : desired) {
                if (!previous.contains(id)) {
                    ServerPlayerEntity player = byId.get(id);
                    if (player != null) {
                        toAdd.add(player);
                    }
                }
            }

            if (!toAdd.isEmpty()) {
                viewer.networkHandler.sendPacket(PlayerListS2CPacket.entryFromPlayer(toAdd));
            }

            visibleTabEntries.put(viewer.getUuid(), desired);
        }
    }

    private void updatePlayerIndicators(ServerPlayerEntity player, String mapName) {
        Text hint = Text.literal("Aktive Map: " + mapName).formatted(Formatting.YELLOW);
        player.sendMessage(hint, true);
    }
}
