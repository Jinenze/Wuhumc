package xyz.jinenze.wuhumc.init;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.*;
import xyz.jinenze.wuhumc.util.PlayerItemUtil;

import java.util.Iterator;
import java.util.function.Supplier;

public class ModServerActions {
    public static final Actions<ServerPlayer> NULL_PLAYER = Actions.<ServerPlayer>getBuilder().action((player, handler) -> true).build();
    public static final Actions<ServerActionContext> NULL_WORLD = Actions.<ServerActionContext>getBuilder().action((player, handler) -> true).build();

    public static final Actions<ServerPlayer> test = Actions.<ServerPlayer>getBuilder().action((player, handler) -> {
        player.level().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), "/me LLLLLL");
        return true;
    }).build();

    public static final ActionProvider<ServerPlayer> dumbActions = () -> new Iterator<>() {
        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Action<ServerPlayer> next() {
            return (player, handler) -> {
                player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1f, 0.5f);
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1f, 0.5f, 9));
                handler.setDelay(1);
                return false;
            };
        }
    };

    private static void ejectPlayer(ServerPlayer player) {
        var config = Wuhumc.config.GAME_POSITION_WSNZ;
        player.teleportTo(config.x, config.y, config.z);
        player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), new Vec3(Wuhumc.config.game_start_player_eject_direction ? 1 : -1, 0.5, 0)));
        Wuhumc.config.game_start_player_eject_direction = !Wuhumc.config.game_start_player_eject_direction;
    }

    public static final Actions<ServerPlayer> RESPAWN_FLY = Actions.<ServerPlayer>getBuilder().action((player, handler) -> {
        if (Wuhumc.config.respawnFlyEnabled) {
            player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), new Vec3(0, 10, 0)));
            return false;
        }
        return true;
    }).wait(65).action((player, handler) -> {
        if (player.getRespawnConfig() != null) {
            var pos = player.getRespawnConfig().respawnData().pos().getBottomCenter();
            player.teleportTo(pos.x(), pos.y(), pos.z());
        } else {
            var pos = player.level().getRespawnData().pos().getBottomCenter();
            player.teleportTo(pos.x(), pos.y(), pos.z());
        }
        return true;
    }).build();

    public static final Actions<ServerActionContext> GAME_END = Actions.<ServerActionContext>getBuilder().action((context, handler) -> {
        var maxProcessor = context.processors().getFirst();
        int maxInteger = Integer.MIN_VALUE;
        for (var processor : context.processors()) {
            if (processor.getScore() > maxInteger) {
                maxInteger = processor.getScore();
                maxProcessor = processor;
            }
        }

        var level = maxProcessor.getPlayer().level();
        if (maxProcessor.getPlayer().getInventory().getItem(EquipmentSlot.HEAD.getIndex(36)).getItem().equals(Items.TURTLE_HELMET)) {
            var turtle = new Turtle(EntityType.TURTLE, level);
            turtle.setCustomName(Component.literal(maxProcessor.getPlayer().getGameProfile().name()));
            turtle.setPos(level.getRespawnData().pos().getBottomCenter());
            turtle.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, -1, 255, false, false));
            level.addFreshEntity(turtle);
        } else {
            var itemStack = new ItemStack(Items.TURTLE_HELMET);
            itemStack.enchant(level.registryAccess().lookupOrThrow(Enchantments.BINDING_CURSE.registryKey()).getOrThrow(Enchantments.BINDING_CURSE), 1);
            maxProcessor.getPlayer().getInventory().setItem(EquipmentSlot.HEAD.getIndex(36), itemStack);
        }
        for (ServerPlayer player : maxProcessor.getPlayer().level().getServer().getPlayerList().getPlayers()) {
            if (ProcessorManager.get(player).getCurrentGame().equals(maxProcessor.getCurrentGame())) {
                ProcessorManager.get(player).setCurrentGame(ModGames.NULL);
                player.setRespawnPosition(null, false);
                var pos = level.getRespawnData().pos().getBottomCenter();
                player.setGameMode(GameType.ADVENTURE);
                player.teleportTo(pos.x(), pos.y(), pos.z());
            }
        }
        maxProcessor.getCurrentGame().gameEnd();
        return true;
    }).build();

    public static final Actions<ServerActionContext> WSNZ_1 = Actions.<ServerActionContext>getBuilder().action((context, handler) -> {
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_wsnz_1")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            processor.emitListener(ModEventListeners.PLAYER_FALL_VOID_WSNZ_1);
        }
        return false;
    }).wait(60).action((context, handler) -> {
        for (var processor : context.processors()) {
            processor.removeListener(ModEventListeners.PLAYER_FALL_VOID_WSNZ_1);
            ejectPlayer(processor.getPlayer());
        }
        return true;
    }).build();

    public static final Actions<ServerActionContext> WSMZ_REFRESH = Actions.<ServerActionContext>getBuilder().action((context, handler) -> {
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_wsnz_refresh_1")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        return false;
    }).wait(10).action((context, handler) -> {
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_wsnz_refresh_2")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        return false;
    }).wait(10).action((context, handler) -> {
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_wsnz_refresh_3")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        return false;
    }).wait(30).action((context, handler) -> {
        var action = ModGames.WSNZ.getIterator().next();
        ProcessorManager.getServerProcessor().emitActions(context, action);
        return true;
    }).build();

    public static final ActionProvider<ServerActionContext> WSNZ_LOOP = () -> new Iterator<>() {
        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Action<ServerActionContext> next() {
            return (context, handler) -> {
                if (ModGames.WSNZ.getIterator().hasNext()) {
                    ProcessorManager.getServerProcessor().emitActions(context, WSMZ_REFRESH);
                    handler.setDelay(120);
                    return false;
                }
                ProcessorManager.getServerProcessor().emitActions(context, GAME_END);
                return true;
            };
        }
    };

    public static final Actions<ServerActionContext> WSNZ_MAIN = Actions.<ServerActionContext>getBuilder().action((context, handler) -> {
        for (var processor : context.processors()) {
            ejectPlayer(processor.getPlayer());
        }
        return false;
    }).wait(20).action(((context, handler) -> {
        ProcessorManager.getServerProcessor().emitActions(context, WSNZ_LOOP);
        return true;
    })).build();

    private static boolean countdown(ServerActionContext context, String key) {
        for (var processor : context.processors()) {
            if (processor.event(processor.getCurrentGame().getOnReadyEvent())) {
                for (var anotherProcessor : context.processors()) {
                    anotherProcessor.getPlayer().connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_cancel")));
                }
                return true;
            }
        }
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(key)));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        return false;
    }

    public static final Actions<ServerActionContext> GAME_COUNTDOWN = Actions.<ServerActionContext>getBuilder(
    ).action((context, handler) -> {
                for (var processor : context.processors()) {
                    var player = processor.getPlayer();
                    player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_countdown_5")));
                    player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
                }
                return false;
            }
    ).wait(20).action((context, handler) -> countdown(context, "title.wuhumc.game_countdown_4")
    ).wait(20).action((context, handler) -> countdown(context, "title.wuhumc.game_countdown_3")
    ).wait(20).action((context, handler) -> countdown(context, "title.wuhumc.game_countdown_2")
    ).wait(20).action((context, handler) -> countdown(context, "title.wuhumc.game_countdown_1")
    ).wait(20).action((context, handler) -> countdown(context, "title.wuhumc.game_countdown_end")
    ).action((context, handler) -> {
                context.processors().forEach(processor -> PlayerItemUtil.removeReadyItemFromPlayer(processor.getPlayer()));
                ProcessorManager.getServerProcessor().planToRemoveRunningActions(new Supplier<>() {
                    @Override
                    public ActionProvider<ServerActionContext> get() {
                        return GAME_COUNTDOWN;
                    }
                });
                context.processors().getFirst().getCurrentGame().gameStart();
                ProcessorManager.getServerProcessor().emitActions(context, context.processors().getFirst().getCurrentGame().getGameStartAction());
                return true;
            }
    ).build();

    public static void register() {
    }
}
