package com.mapswitch.teleport;

import com.mapswitch.map.MapContext;
import com.mapswitch.map.MapManager;
import com.mapswitch.player.PlayerDataManager;
import com.mapswitch.world.exception.RegistryFailureException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;

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

    public void switchPlayerToMap(ServerPlayer player, MapContext targetMap) {
        MapContext oldMap = resolveCurrentMap(player);
        if (oldMap != null) {
            playerDataManager.savePlayerForMap(player, oldMap);
        }

        playerDataManager.loadPlayerForMap(player, targetMap);
        ServerLevel targetWorld = mapManager.resolveTargetWorld(targetMap);
        ensureSpawnChunkLoaded(targetWorld);

        player.teleportTo(targetWorld, 0.5, 100.0, 0.5, Set.<net.minecraft.world.entity.Relative>of(), player.getYRot(), player.getXRot(), false);
        playerActiveMap.put(player.getUUID(), targetMap.name());
        updatePlayerIndicators(player, targetMap.name());
        refreshTabVisibility(targetWorld.getServer());
        player.setInvulnerableTime(INVUL_TICKS);
        invulnerabilityTicks.put(player.getUUID(), INVUL_TICKS);
    }

    private MapContext resolveCurrentMap(ServerPlayer player) {
        String current = playerActiveMap.get(player.getUUID());
        if (current == null) {
            return null;
        }
        return mapManager.getMap(current).orElse(null);
    }

    private void ensureSpawnChunkLoaded(ServerLevel world) {
        try {
            BlockPos spawn = BlockPos.ZERO;
            world.setChunkForced(spawn.getX() >> 4, spawn.getZ() >> 4, true);
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
            ServerPlayer player = server.getPlayerList().getPlayer(next.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }
            int left = next.getValue() - 1;
            if (left <= 0) {
                player.setInvulnerableTime(0);
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

    public void ensurePlayerMapHint(ServerPlayer player, String defaultMap) {
        if (playerActiveMap.containsKey(player.getUUID())) {
            updatePlayerIndicators(player, playerActiveMap.get(player.getUUID()));
            return;
        }
        playerActiveMap.put(player.getUUID(), defaultMap);
        updatePlayerIndicators(player, defaultMap);
    }

    private void refreshTabVisibility(MinecraftServer server) {
        if (server == null) {
            return;
        }
        Collection<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            visibleTabEntries.clear();
            return;
        }

        Map<UUID, ServerPlayer> byId = new HashMap<>();
        for (ServerPlayer player : players) {
            byId.put(player.getUUID(), player);
            playerActiveMap.putIfAbsent(player.getUUID(), mapManager.getMaps().stream().findFirst().map(m -> m.name()).orElse("normal"));
        }

        for (ServerPlayer viewer : players) {
            String viewerMap = playerActiveMap.getOrDefault(viewer.getUUID(), "normal");
            Set<UUID> desired = new HashSet<>();
            for (ServerPlayer other : players) {
                String otherMap = playerActiveMap.getOrDefault(other.getUUID(), "normal");
                if (viewerMap.equalsIgnoreCase(otherMap)) {
                    desired.add(other.getUUID());
                }
            }

            Set<UUID> previous = visibleTabEntries.getOrDefault(viewer.getUUID(), Set.of());
            List<UUID> toRemove = new ArrayList<>();
            for (UUID id : previous) {
                if (!desired.contains(id)) {
                    toRemove.add(id);
                }
            }

            if (!toRemove.isEmpty()) {
                viewer.connection.send(new ClientboundPlayerInfoRemovePacket(toRemove));
            }

            List<ServerPlayer> toAdd = new ArrayList<>();
            for (UUID id : desired) {
                if (!previous.contains(id)) {
                    ServerPlayer player = byId.get(id);
                    if (player != null) {
                        toAdd.add(player);
                    }
                }
            }

            if (!toAdd.isEmpty()) {
                viewer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(toAdd));
            }

            visibleTabEntries.put(viewer.getUUID(), desired);
        }
    }

    private void updatePlayerIndicators(ServerPlayer player, String mapName) {
        Component hint = Component.literal("Aktive Map: " + mapName).withStyle(ChatFormatting.YELLOW);
        player.sendSystemMessage(hint, true);
    }
}

