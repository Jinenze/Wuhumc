package xyz.jinenze.wuhumc.init;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.ActionProvider;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.config.ServerConfig;
import xyz.jinenze.wuhumc.game.Game;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("wuhumc"
                ).then(literal("action").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                        .then(literal("dumbactions").then(emitActions(ModServerActions.dumbActions)))
                        .then(literal("test").then(emitActions(ModServerActions.test)))
                        .then(literal("clear").then(argument("targets", EntityArgumentType.players())
                                .executes(context -> {
                                    Collection<ServerPlayerEntity> collection = EntityArgumentType.getPlayers(context, "targets");
                                    if (!collection.isEmpty()) {
                                        for (ServerPlayerEntity player : collection) {
                                            ProcessorManager.get(player).clearActions();
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
                                            ProcessorManager.get(player).clearListeners();
                                        }
                                    }
                                    return collection.size();
                                })))
                ).then(literal("game").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                        .then(literal("nswz").then(setGame(ModGames.WSNZ)))
                ).then(literal("config").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                        .then(literal("respawnfly").then(argument("bool", BoolArgumentType.bool()).executes(context -> {
                            Wuhumc.config.respawnFlyEnabled = (BoolArgumentType.getBool(context, "bool"));
                            return 1;
                        })))
                        .then(literal("nswzposition").then(setGamePosition(Wuhumc.config.GAME_POSITION_WSNZ)))
                )
        ));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> emitActions(ActionProvider<ServerPlayerEntity> actions) {
        return argument("targets", EntityArgumentType.players())
                .executes(context -> {
                    Collection<ServerPlayerEntity> collection = EntityArgumentType.getPlayers(context, "targets");
                    if (!collection.isEmpty()) {
                        for (ServerPlayerEntity player : collection) {
                            ProcessorManager.get(player).emitActions(actions);
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
                            ProcessorManager.get(player).emitListener(listener);
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
                            if (game.isRunning()) {
                                player.changeGameMode(GameMode.SPECTATOR);
                                ProcessorManager.get(player).setCurrentGame(game);
                            } else {
                                ProcessorManager.get(player).emitListener(game.getNotReadyListener());
                                ProcessorManager.get(player).setCurrentGame(game);
                            }
                        }
                    }
                    return collection.size();
                });
    }

    private static ArgumentBuilder<ServerCommandSource, ?> setGamePosition(ServerConfig.GamePosition config) {
        return argument("position", Vec3ArgumentType.vec3())
                .executes(context -> {
                    var pos = Vec3ArgumentType.getPosArgument(context, "position").getPos(context.getSource());
                    config.x = pos.x;
                    config.y = pos.y;
                    config.z = pos.z;
                    return 1;
                });
    }
}
