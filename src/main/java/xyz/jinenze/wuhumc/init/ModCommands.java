package xyz.jinenze.wuhumc.init;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.ActionProvider;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.config.ServerConfig;
import xyz.jinenze.wuhumc.game.Game;
import xyz.jinenze.wuhumc.network.Payloads;
import xyz.jinenze.wuhumc.util.PlayerUtil;

import java.util.Collection;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("wuhumc"
                ).then(literal("action").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(literal("dumbactions").then(emitPlayerActions(ModServerActions.dumbActions)))
                        .then(literal("clearreadyitem").then(emitPlayerActions(ModServerActions.clearReadyItem)))
                        .then(literal("newmoniter").executes(context -> {
                            context.getSource().getPlayer().sendSystemMessage(Component.literal("" + context.getSource().getPlayer().getXRot()));
                            context.getSource().getPlayer().sendSystemMessage(Component.literal("" + context.getSource().getPlayer().getYRot()));
                            context.getSource().getPlayer().sendSystemMessage(Component.literal("" + context.getSource().getPlayer().getYHeadRot()));
                            return 1;
                        }))
                        .then(literal("clear").then(argument("targets", EntityArgument.players())
                                .executes(context -> {
                                    Collection<ServerPlayer> collection = EntityArgument.getPlayers(context, "targets");
                                    if (!collection.isEmpty()) {
                                        for (ServerPlayer player : collection) {
                                            ProcessorManager.get(player).clearActions();
                                        }
                                    }
                                    return collection.size();
                                })).then(literal("server").executes(context -> {
                            ProcessorManager.getServerProcessor().clearActions();
                            return 1;
                        })))
                ).then(literal("listener").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(literal("clear").then(argument("targets", EntityArgument.players())
                                .executes(context -> {
                                    Collection<ServerPlayer> collection = EntityArgument.getPlayers(context, "targets");
                                    if (!collection.isEmpty()) {
                                        for (ServerPlayer player : collection) {
                                            ProcessorManager.get(player).clearListeners();
                                        }
                                    }
                                    return collection.size();
                                })).then(literal("server").executes(context -> {
                            ProcessorManager.getServerProcessor().clearListeners();
                            return 1;
                        })))
                ).then(literal("game").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(literal("nswz").then(setPlayerGame(ModGames.WSNZ)))
                ).then(literal("config").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(literal("respawnfly").then(argument("bool", BoolArgumentType.bool()).executes(context -> {
                            Wuhumc.config.respawn_fly_enabled = (BoolArgumentType.getBool(context, "bool"));
                            return 1;
                        })))
                        .then(literal("respawnmusic")
                                .then(literal("false").then(setPlayerConfig(false)))
                                .then(literal("true").then(setPlayerConfig(true))))
                        .then(literal("nswzposition").then(setGamePosition(Wuhumc.config.game_settings_wsnz.game_position_wsnz)))
                        .then(literal("download").executes(context -> {
                            if (context.getSource().getPlayer() != null) {
                                ServerPlayNetworking.send(context.getSource().getPlayer(), new Payloads.ServerConfigC2SPayload(Wuhumc.config));
                            }
                            return 1;
                        }))
                ).then(literal("test").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(literal("scoreboard").then(emitPlayerActions(ModServerActions.test)))
                        .then(literal("clearinventory").then(emitPlayerActions(ModServerActions.test1)))
                )
        ));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> emitPlayerActions(ActionProvider<ServerPlayer> actions) {
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

    private static ArgumentBuilder<CommandSourceStack, ?> setPlayerGame(Game game) {
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
                                PlayerUtil.placeReadyItemToIndexZero(player);
                            }
                        }
                    }
                    return collection.size();
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> setPlayerConfig(boolean bl) {
        return argument("targets", EntityArgument.players())
                .executes(context -> {
                    Collection<ServerPlayer> collection = EntityArgument.getPlayers(context, "targets");
                    if (!collection.isEmpty()) {
                        for (ServerPlayer player : collection) {
                            ServerPlayNetworking.send(player, new Payloads.SetRespawnMusicS2CPayload(bl));
                        }
                    }
                    return collection.size();
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> setGamePosition(ServerConfig.GamePosition config) {
        return argument("position", BlockPosArgument.blockPos())
                .executes(context -> {
                    var pos = BlockPosArgument.getBlockPos(context, "position");
                    config.x = pos.getX();
                    config.y = pos.getY();
                    config.z = pos.getZ();
                    return 1;
                });
    }
}
