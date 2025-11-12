package xyz.jinenze.wuhumc.init;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.ActionProvider;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.config.ServerConfig;
import xyz.jinenze.wuhumc.game.Game;

import java.util.Collection;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("wuhumc"
                ).then(literal("action").requires(serverCommandSource -> serverCommandSource.hasPermission(2))
                        .then(literal("dumbactions").then(emitActions(ModServerActions.dumbActions)))
                        .then(literal("test").then(emitActions(ModServerActions.test)))
                        .then(literal("clear").then(argument("targets", EntityArgument.players())
                                .executes(context -> {
                                    Collection<ServerPlayer> collection = EntityArgument.getPlayers(context, "targets");
                                    if (!collection.isEmpty()) {
                                        for (ServerPlayer player : collection) {
                                            ProcessorManager.get(player).clearActions();
                                        }
                                    }
                                    return collection.size();
                                })))
                ).then(literal("listener").requires(serverCommandSource -> serverCommandSource.hasPermission(2))
                        .then(literal("nswznotready").then(listen(ModEventListeners.PLAYER_WSNZ_READY_PLAYER_NOT_READY)))
                        .then(literal("clear").then(argument("targets", EntityArgument.players())
                                .executes(context -> {
                                    Collection<ServerPlayer> collection = EntityArgument.getPlayers(context, "targets");
                                    if (!collection.isEmpty()) {
                                        for (ServerPlayer player : collection) {
                                            ProcessorManager.get(player).clearListeners();
                                        }
                                    }
                                    return collection.size();
                                })))
                ).then(literal("game").requires(serverCommandSource -> serverCommandSource.hasPermission(2))
                        .then(literal("nswz").then(setGame(ModGames.WSNZ)))
                ).then(literal("config").requires(serverCommandSource -> serverCommandSource.hasPermission(2))
                        .then(literal("respawnfly").then(argument("bool", BoolArgumentType.bool()).executes(context -> {
                            Wuhumc.config.respawnFlyEnabled = (BoolArgumentType.getBool(context, "bool"));
                            return 1;
                        })))
                        .then(literal("nswzposition").then(setGamePosition(Wuhumc.config.GAME_POSITION_WSNZ)))
                )
        ));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> emitActions(ActionProvider<ServerPlayer> actions) {
        return argument("targets", EntityArgument.players())
                .executes(context -> {
                    Collection<ServerPlayer> collection = EntityArgument.getPlayers(context, "targets");
                    if (!collection.isEmpty()) {
                        for (ServerPlayer player : collection) {
                            ProcessorManager.get(player).emitActions(actions);
                        }
                    }
                    return collection.size();
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> listen(EventListener<ServerPlayer> listener) {
        return argument("targets", EntityArgument.players())
                .executes(context -> {
                    Collection<ServerPlayer> collection = EntityArgument.getPlayers(context, "targets");
                    if (!collection.isEmpty()) {
                        for (ServerPlayer player : collection) {
                            ProcessorManager.get(player).emitListener(listener);
                        }
                    }
                    return collection.size();
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> setGame(Game game) {
        return argument("targets", EntityArgument.players())
                .executes(context -> {
                    Collection<ServerPlayer> collection = EntityArgument.getPlayers(context, "targets");
                    if (!collection.isEmpty()) {
                        for (ServerPlayer player : collection) {
                            if (game.isRunning()) {
                                player.setGameMode(GameType.SPECTATOR);
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

    private static ArgumentBuilder<CommandSourceStack, ?> setGamePosition(ServerConfig.GamePosition config) {
        return argument("position", Vec3Argument.vec3())
                .executes(context -> {
                    var pos = Vec3Argument.getVec3(context, "position");
                    config.x = pos.x;
                    config.y = pos.y;
                    config.z = pos.z;
                    return 1;
                });
    }
}
