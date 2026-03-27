package com.mapswitch.command;

import com.mapswitch.MapSwitchMod;
import com.mapswitch.map.MapContext;
import com.mapswitch.map.MapManager;
import com.mapswitch.teleport.TeleportUtils;
import com.mapswitch.world.exception.MapSwitchException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.command.CommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class MapSwitchCommand {
    private MapSwitchCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("map")
                        .then(literal("switch")
                                .then(argument("mapname", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(
                                                MapSwitchMod.mapManager() == null ? java.util.List.of() : MapSwitchMod.mapManager().getLoadedMapNames(),
                                                builder))
                                        .executes(ctx -> executeSwitch(ctx.getSource(), StringArgumentType.getString(ctx, "mapname")))))
        );

        dispatcher.register(
                literal("mapswitch")
                        .then(argument("mapname", StringArgumentType.word())
                                .suggests((context, builder) -> CommandSource.suggestMatching(
                                        MapSwitchMod.mapManager() == null ? java.util.List.of() : MapSwitchMod.mapManager().getLoadedMapNames(),
                                        builder))
                                .executes(ctx -> executeSwitch(ctx.getSource(), StringArgumentType.getString(ctx, "mapname"))))
        );
    }

    private static int executeSwitch(ServerCommandSource source, String mapName) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception ex) {
            source.sendError(Text.literal("Nur Spieler können diesen Befehl ausführen."));
            return 0;
        }

        MapManager mapManager = MapSwitchMod.mapManager();
        TeleportUtils teleportUtils = MapSwitchMod.teleportUtils();
        if (mapManager == null || teleportUtils == null) {
            source.sendError(Text.literal("MapSwitch ist noch nicht initialisiert."));
            return 0;
        }

        try {
            MapContext target = mapManager.getMapOrThrow(mapName);
            teleportUtils.switchPlayerToMap(player, target);
            source.sendFeedback(() -> Text.literal("Du bist zu Map '" + mapName + "' gewechselt."), false);
            return 1;
        } catch (MapSwitchException ex) {
            source.sendError(Text.literal("MapSwitch Fehler: " + ex.getMessage()));
            return 0;
        } catch (Exception ex) {
            source.sendError(Text.literal("Unerwarteter Fehler beim Mapwechsel."));
            MapSwitchMod.LOGGER.error("[MapSwitch] command failed", ex);
            return 0;
        }
    }
}
