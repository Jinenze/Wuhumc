package xyz.jinenze.wuhumc.init;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.ActionSupplier;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.action.ServerActionContext;
import xyz.jinenze.wuhumc.action.ServerPlayerProcessor;
import xyz.jinenze.wuhumc.config.ServerConfigWrapper;
import xyz.jinenze.wuhumc.game.GameSession;
import xyz.jinenze.wuhumc.game.OvereatingGame;
import xyz.jinenze.wuhumc.network.Payloads;
import xyz.jinenze.wuhumc.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("wuhumc"
                ).then(literal("action").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(literal("dumbactions").then(emitPlayerActions(ModServerActions.dumbActions)))
                        .then(literal("clearreadyitem").then(emitPlayerActions(ModServerActions.clearReadyItem)))
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
                        .then(literal("null").then(setPlayerGameSession(() -> ModGames.NULL)))
                        .then(literal("nswz").then(setPlayerGameSession(() -> ModGames.WSNZ)))
                        .then(argument("targets", EntityArgument.players())
                                .executes(context -> {
                                    Collection<ServerPlayer> collection = EntityArgument.getPlayers(context, "targets");
                                    if (!collection.isEmpty() && context.getSource().getPlayer() != null) {
                                        for (ServerPlayer player : collection) {
                                            context.getSource().getPlayer().sendSystemMessage(Component.literal(ProcessorManager.get(player).getGameSession().getGameData().toString()));
                                        }
                                    }
                                    return collection.size();
                                }))
                ).then(literal("config").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(literal("respawnfly")
                                .then(argument("bool", BoolArgumentType.bool()).executes(context -> {
                                    Wuhumc.config.respawn_fly_enabled = (BoolArgumentType.getBool(context, "bool"));
                                    AutoConfig.getConfigHolder(ServerConfigWrapper.class).save();
                                    return 1;
                                }))
                                .executes(context -> {
                                    if (context.getSource().getPlayer() != null) {
                                        context.getSource().getPlayer().sendSystemMessage(Component.literal(String.valueOf(Wuhumc.config.respawn_fly_enabled)));
                                    }
                                    return 1;
                                })
                        )
                        .then(literal("respawnmusic")
                                .then(argument("targets", EntityArgument.players())
                                        .then(argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    Collection<ServerPlayer> collection = EntityArgument.getPlayers(context, "targets");
                                                    if (!collection.isEmpty()) {
                                                        for (ServerPlayer player : collection) {
                                                            ServerPlayNetworking.send(player, new Payloads.SetRespawnMusicS2CPayload(BoolArgumentType.getBool(context, "value")));
                                                        }
                                                    }
                                                    return collection.size();
                                                }))))
                        .then(literal("nswzposition")
                                .then(argument("position", BlockPosArgument.blockPos()).executes(context -> {
                                    Wuhumc.config.game_settings_wsnz.position = BlockPosArgument.getBlockPos(context, "position").above(1);
                                    AutoConfig.getConfigHolder(ServerConfigWrapper.class).save();
                                    return 1;
                                }))
                                .executes(context -> {
                                    if (context.getSource().getPlayer() != null) {
                                        context.getSource().getPlayer().sendSystemMessage(Component.literal(Wuhumc.config.game_settings_wsnz.position.toString()));
                                    }
                                    return 1;
                                }))
                        .then(literal("download").executes(context -> {
                            if (context.getSource().getPlayer() != null) {
                                ServerPlayNetworking.send(context.getSource().getPlayer(), new Payloads.ServerConfigS2CPayload(Wuhumc.config));
                            }
                            return 1;
                        }))
                ).then(literal("test").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(literal("scoreboard").then(emitPlayerActions(ModServerActions.test)))
                        .then(literal("clearinventory").then(emitPlayerActions(ModServerActions.test1)))
                        .then(literal("overeating")
                                .then(argument("targets", EntityArgument.players())
                                        .executes(context -> {
                                            Collection<ServerPlayer> collection = EntityArgument.getPlayers(context, "targets");
                                            if (!collection.isEmpty()) {
                                                var list = new ArrayList<ServerPlayerProcessor>();
                                                for (ServerPlayer player : collection) {
                                                    list.add(ProcessorManager.get(player));
                                                }
                                                ProcessorManager.getServerProcessor().emitActions(new ServerActionContext(List.copyOf(list)), OvereatingGame.OVEREATING_PRESET_ONE);
                                            }
                                            return collection.size();
                                        })))
                ).then(literal("client")
                        .then(literal("joinwsnz").executes(clientPlayerJoinGame(ModGames.WSNZ)))
                        .then(literal("joinovereating").executes(clientPlayerJoinGame(ModGames.OVEREATING)))
                )
        ));
    }

    private static Command<CommandSourceStack> clientPlayerJoinGame(GameSession session) {
        return context -> {
            if (context.getSource().getPlayer() != null && !Util.isPlayerInGame(context.getSource().getPlayer())) {
                Util.setPlayerGameSession(context.getSource().getPlayer(), session);
            }
            return 1;
        };
    }

    private static ArgumentBuilder<CommandSourceStack, ?> emitPlayerActions(ActionSupplier<ServerPlayer> actions) {
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

    private static ArgumentBuilder<CommandSourceStack, ?> setPlayerGameSession(Supplier<GameSession> getter) {
        return argument("targets", EntityArgument.players())
                .executes(context -> {
                    Collection<ServerPlayer> collection = EntityArgument.getPlayers(context, "targets");
                    if (!collection.isEmpty()) {
                        for (ServerPlayer player : collection) {
                            Util.setPlayerGameSession(player, getter.get());
                        }
                    }
                    return collection.size();
                });
    }
}
