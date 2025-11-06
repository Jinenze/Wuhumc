package xyz.jinenze.wuhumc.init;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.ActionProvider;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.Game;
import xyz.jinenze.wuhumc.action.ProcessorManager;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("wuhumc")
                        .then(literal("action").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .then(literal("gamecounterdown").then(emitActions(ModServerActions.GAME_COUNTDOWN)))
                                .then(literal("dumbactions").then(emitActions(ModServerActions.dumbActions)))
                                .then(literal("test").then(emitActions(ModServerActions.test)))
                                .then(literal("clear").then(argument("targets", EntityArgumentType.players())
                                        .executes(context -> {
                                            Collection<ServerPlayerEntity> collection = EntityArgumentType.getPlayers(context, "targets");
                                            if (!collection.isEmpty()) {
                                                for (ServerPlayerEntity player : collection) {
                                                    ProcessorManager.getInstance().get(player).clearActions();
                                                }
                                            }
                                            return collection.size();
                                        })))
                        ).then(literal("listener").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .then(literal("nswznotready").then(listen(ModEventListeners.PLAYER_WSNZ_READY_PLAYER_NOT_READY)))
                                .then(literal("clear").then(argument("targets", EntityArgumentType.players())
                                        .executes(context -> {
                                            Collection<ServerPlayerEntity> collection = EntityArgumentType.getPlayers(context, "targets");
                                            if (!collection.isEmpty()) {
                                                for (ServerPlayerEntity player : collection) {
                                                    ProcessorManager.getInstance().get(player).clearListeners();
                                                }
                                            }
                                            return collection.size();
                                        })))
                        ).then(literal("game").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .then(literal("nswz").then(setGame(ModGames.WSNZ)))
                        ).then(literal("config").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .then(literal("respawnfly").then(argument("bool", BoolArgumentType.bool()).executes(context -> {
                                    Wuhumc.config.setRespawnFlyEnabled(BoolArgumentType.getBool(context, "bool"));
                                    return 1;
                                })))
                        )
        ));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> emitActions(ActionProvider<ServerPlayerEntity> actions) {
        return argument("targets", EntityArgumentType.players())
                .executes(context -> {
                    Collection<ServerPlayerEntity> collection = EntityArgumentType.getPlayers(context, "targets");
                    if (!collection.isEmpty()) {
                        for (ServerPlayerEntity player : collection) {
                            ProcessorManager.getInstance().get(player).emitActions(actions);
                        }
                    }
                    return collection.size();
                });
    }

    private static ArgumentBuilder<ServerCommandSource, ?> listen(EventListener<ServerPlayerEntity> listener) {
        return argument("targets", EntityArgumentType.players())
                .executes(context -> {
                    Collection<ServerPlayerEntity> collection = EntityArgumentType.getPlayers(context, "targets");
                    if (!collection.isEmpty()) {
                        for (ServerPlayerEntity player : collection) {
                            ProcessorManager.getInstance().get(player).emitListener(listener);
                        }
                    }
                    return collection.size();
                });
    }

    private static ArgumentBuilder<ServerCommandSource, ?> setGame(Game game) {
        return argument("targets", EntityArgumentType.players())
                .executes(context -> {
                    Collection<ServerPlayerEntity> collection = EntityArgumentType.getPlayers(context, "targets");
                    if (!collection.isEmpty()) {
                        for (ServerPlayerEntity player : collection) {
                            ProcessorManager.getInstance().get(player).emitListener(game.notReadyListener());
                            ProcessorManager.getInstance().get(player).setCurrentGame(game);
                        }
                    }
                    return collection.size();
                });
    }
}
