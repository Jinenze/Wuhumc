package xyz.jinenze.wuhumc.init;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.*;
import xyz.jinenze.wuhumc.util.PlayerItemUtil;

import java.util.Iterator;
import java.util.function.Supplier;

public class ModServerActions {
    public static final Actions<ServerPlayerEntity> NULL_PLAYER = Actions.<ServerPlayerEntity>getBuilder().action((player, handler) -> true).build();
    public static final Actions<ServerActionContext> NULL_WORLD = Actions.<ServerActionContext>getBuilder().action((player, handler) -> true).build();

    public static final Actions<ServerPlayerEntity> test = Actions.<ServerPlayerEntity>getBuilder().action((player, handler) -> {
        player.getEntityWorld().getServer().getCommandManager().parseAndExecute(player.getCommandSource(), "/me LLLLLL");
        return true;
    }).build();

    public static final ActionProvider<ServerPlayerEntity> dumbActions = () -> new Iterator<>() {
        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Action<ServerPlayerEntity> next() {
            return (player, handler) -> {
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 1f, 0.5f);
                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 1f, 0.5f, 9));
                handler.setDelay(1);
                return false;
            };
        }
    };

    private static void ejectPlayer(ServerPlayerEntity player) {
        var config = Wuhumc.config.GAME_POSITION_WSNZ;
        player.teleport(config.x, config.y, config.z, false);
        player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player.getId(), new Vec3d(Wuhumc.config.game_start_player_eject_direction ? 1 : -1, 0.5, 0)));
        Wuhumc.config.game_start_player_eject_direction = !Wuhumc.config.game_start_player_eject_direction;
    }

    public static final Actions<ServerPlayerEntity> RESPAWN_FLY = Actions.<ServerPlayerEntity>getBuilder().action((player, handler) -> {
        if (Wuhumc.config.respawnFlyEnabled) {
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player.getId(), new Vec3d(0, 10, 0)));
            return false;
        }
        return true;
    }).wait(65).action((player, handler) -> {
        if (player.getRespawn() != null) {
            var pos = player.getRespawn().respawnData().getPos().toBottomCenterPos();
            player.teleport(pos.getX(), pos.getY(), pos.getZ(), false);
        } else {
            var pos = player.getEntityWorld().getSpawnPoint().getPos();
            player.teleport(pos.getX(), pos.getY(), pos.getZ(), false);
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

        var world = maxProcessor.getPlayer().getEntityWorld();
        if (maxProcessor.getPlayer().getInventory().getStack(EquipmentSlot.HEAD.getOffsetEntitySlotId(36)).getItem().equals(Items.TURTLE_HELMET)) {
            var turtle = new TurtleEntity(EntityType.TURTLE, world);
            turtle.setCustomName(Text.literal(maxProcessor.getPlayer().getGameProfile().name()));
            turtle.setPosition(world.getSpawnPoint().getPos().toBottomCenterPos());
            turtle.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, -1, 255, false, false));
            world.spawnEntity(turtle);
        } else {
            var itemStack = new ItemStack(Items.TURTLE_HELMET);
            itemStack.addEnchantment(world.createCommandRegistryWrapper(Enchantments.BINDING_CURSE.getRegistryRef()).getOrThrow(Enchantments.BINDING_CURSE), 1);
            maxProcessor.getPlayer().getInventory().setStack(EquipmentSlot.HEAD.getOffsetEntitySlotId(36), itemStack);
        }
        for (ServerPlayerEntity player : maxProcessor.getPlayer().getEntityWorld().getServer().getPlayerManager().getPlayerList()) {
            if (ProcessorManager.get(player).getCurrentGame().equals(maxProcessor.getCurrentGame())) {
                ProcessorManager.get(player).setCurrentGame(ModGames.NULL);
                player.setSpawnPoint(null, false);
                var pos = world.getSpawnPoint().getPos();
                player.changeGameMode(GameMode.ADVENTURE);
                player.teleport(pos.getX(), pos.getY(), pos.getZ(), false);
            }
        }
        maxProcessor.getCurrentGame().gameEnd();
        return true;
    }).build();

    public static final Actions<ServerActionContext> WSNZ_1 = Actions.<ServerActionContext>getBuilder().action((context, handler) -> {
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.wuhumc.game_wsnz_1")));
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
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
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.wuhumc.game_wsnz_refresh_1")));
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        return false;
    }).wait(10).action((context, handler) -> {
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.wuhumc.game_wsnz_refresh_2")));
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        return false;
    }).wait(10).action((context, handler) -> {
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.wuhumc.game_wsnz_refresh_3")));
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
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
                    anotherProcessor.getPlayer().networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.wuhumc.game_cancel")));
                }
                return true;
            }
        }
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable(key)));
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        return false;
    }

    public static final Actions<ServerActionContext> GAME_COUNTDOWN = Actions.<ServerActionContext>getBuilder(
    ).action((context, handler) -> {
                for (var processor : context.processors()) {
                    var player = processor.getPlayer();
                    player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.wuhumc.game_countdown_5")));
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
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
