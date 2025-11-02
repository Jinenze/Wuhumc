package xyz.jinenze.wuhumc.init;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.jinenze.wuhumc.action.ActionProvider;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.Game;
import xyz.jinenze.wuhumc.action.ServerMixinGetter;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("wuhumc")
                        .then(literal("action").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .then(literal("respawnmusic").then(emitActions(ModServerActions.respawnMusic)))
                                .then(literal("dumbactions").then(emitActions(ModServerActions.dumbActions)))
                                .then(literal("test").then(emitActions(ModServerActions.test)))
                                .then(literal("clear").then(argument("targets", EntityArgumentType.players())
                                        .executes(context -> {
                                            Collection<ServerPlayerEntity> collection = EntityArgumentType.getPlayers(context, "targets");
                                            if (!collection.isEmpty()) {
                                                for (ServerPlayerEntity player : collection) {
                                                    ((ServerMixinGetter) player).wuhumc$getProcessor().clearActions();
                                                }
                                            }
                                            return collection.size();
                                        })))
                        ).then(literal("listener").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .then(literal("nswznotready").then(listen(ModEventListeners.WSNZ_NOT_READY)))
                        ).then(literal("game").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .then(literal("nswz").then(setGame(ModGames.WSNZ)))
                        )
        ));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> emitActions(ActionProvider<ServerPlayerEntity> actions) {
        return argument("targets", EntityArgumentType.players())
                .executes(context -> {
                    Collection<ServerPlayerEntity> collection = EntityArgumentType.getPlayers(context, "targets");
                    if (!collection.isEmpty()) {
                        for (ServerPlayerEntity player : collection) {
                            ((ServerMixinGetter) player).wuhumc$getProcessor().emitActions(actions);
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
                            ((ServerMixinGetter) player).wuhumc$getProcessor().listen(listener);
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
                            ((ServerMixinGetter) player).wuhumc$getProcessor().listen(game.notReadyListener());
                            ((ServerMixinGetter) player).wuhumc$setCurrentGame(game);
                        }
                    }
                    return collection.size();
                });
    }
}
