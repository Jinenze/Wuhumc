package xyz.jinenze.wuhumc.init;

import com.ibm.icu.impl.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.*;
import xyz.jinenze.wuhumc.network.Payloads;
import xyz.jinenze.wuhumc.util.InventorySnapshot;
import xyz.jinenze.wuhumc.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public class ModServerActions {
    public static final ActionList<ServerPlayer> NULL_PLAYER = ActionList.<ServerPlayer>getBuilder().action((player, handler) -> true).build();
    public static final ActionList<ServerActionContext> NULL_WORLD = ActionList.<ServerActionContext>getBuilder().action((player, handler) -> true).build();

    public static final ActionList<ServerPlayer> test = ActionList.<ServerPlayer>getBuilder().action((player, handler) -> {
        var data = new HashMap<String, Pair<Integer, Integer>>();
        data.put("test1", Pair.of(1, 2));
        data.put("test2", Pair.of(1, 2));
        data.put("test3", Pair.of(1, 2));
        data.put("test4", Pair.of(1, 2));
        data.put("test5", Pair.of(199, 188));
        data.put("test6", Pair.of(1777, 123));
        data.put("test7", Pair.of(112412, 2111));
        data.put("test8", Pair.of(1, 2123124));
        ServerPlayNetworking.send(player, new Payloads.ShowScoreBoardS2CPayload(data));
        return true;
    }).build();

    public static final ActionList<ServerPlayer> test1 = ActionList.<ServerPlayer>getBuilder().wait(60).action((player, handler) -> {
        player.containerMenu.setCarried(ItemStack.EMPTY);
        player.inventoryMenu.getInputGridSlots().forEach(slot -> slot.set(ItemStack.EMPTY));
        return true;
    }).build();

    public static final ActionList<ServerPlayer> clearReadyItem = ActionList.<ServerPlayer>getBuilder().wait(60).action((player, handler) -> {
        Util.removeReadyItemsFromPlayer(player);
        return true;
    }).build();

    public static final ActionSupplier<ServerPlayer> dumbActions = () -> (HasNextIterator<Action<ServerPlayer>>) () -> (player, handler) -> {
        player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1f, 0.5f);
        player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1f, 0.5f, 0));
        handler.setDelay(1);
        return false;
    };

    public static void showPlayersScoreBoard(ServerActionContext context) {
        Map<String, Pair<Integer, Integer>> map = new HashMap<>();
        context.processors().forEach(processor -> {
            map.put(processor.getPlayer().getGameProfile().name(), Pair.of(processor.getCurrentScore(), processor.getPreviousScore()));
            processor.resetPreviousScore();
        });
        context.processors().forEach(processor -> ServerPlayNetworking.send(processor.getPlayer(), new Payloads.ShowScoreBoardS2CPayload(map)));
    }

    public static ActionSupplier<ServerActionContext> newCountdownAction(int delay) {
        return () -> new HasNextIterator<>() {
            private int count = delay;
            private final Action<ServerActionContext> action = (context, handler) -> {
                for (var processor : context.processors()) {
                    processor.getPlayer().sendSystemMessage(Component.literal("§2" + (count / 20)), true);
                }
                --count;
                handler.setDelay(1);
                return count <= 0;
            };

            @Override
            public Action<ServerActionContext> next() {
                return action;
            }
        };
    }

    public static ActionSupplier<ServerPlayer> newPlayerMonitor(int delay, Function<ServerPlayer, Boolean> monitor) {
        return () -> new HasNextIterator<>() {
            private int lifeTime = delay;
            private final Action<ServerPlayer> action = (player, handler) -> {
                if (monitor.apply(player)) {
                    Util.addScore(player);
                    return true;
                }
                --lifeTime;
                handler.setDelay(1);
                return lifeTime <= 0;
            };

            @Override
            public Action<ServerPlayer> next() {
                return action;
            }
        };
    }

    private static final Map<ActionsHandler<ServerPlayer>, Vec3> DATA_POSITION = new WeakHashMap<>();
    public static final ActionList<ServerPlayer> RESPAWN_FLY = ActionList.<ServerPlayer>getBuilder().action((player, handler) -> {
        if (Wuhumc.config.respawn_fly_enabled) {
            DATA_POSITION.put(handler, player.position());
            player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), new Vec3(0, 10, 0)));
            return false;
        }
        return true;
    }).wait(65).action((player, handler) -> {
        Util.teleportTo(player, DATA_POSITION.get(handler));
        return true;
    }).build();

    public static final ActionList<ServerActionContext> GAME_END = ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
        var maxProcessor = context.processors().getFirst();
        int maxInteger = Integer.MIN_VALUE;
        for (var processor : context.processors()) {
            if (processor.getCurrentScore() > maxInteger) {
                maxInteger = processor.getCurrentScore();
                maxProcessor = processor;
            }
            processor.resetScore();
            processor.getPlayer().getInventory().replaceWith(processor.getInventoryCacheStack().pop().getInventory());
        }

        var level = maxProcessor.getPlayer().level();
        if (maxProcessor.getPlayer().getInventory().getItem(EquipmentSlot.HEAD.getIndex(36)).getItem().equals(Items.TURTLE_HELMET)) {
            var turtle = new Turtle(EntityType.TURTLE, level);
            turtle.setCustomName(Component.literal(maxProcessor.getPlayer().getGameProfile().name()));
            turtle.setPos(level.getRespawnData().pos().getBottomCenter());
            turtle.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, -1, 255, false, false));
            level.addFreshEntity(turtle);
        } else {
            var itemStack = new ItemStack(Items.TURTLE_HELMET);
            itemStack.enchant(level.registryAccess().lookupOrThrow(Enchantments.BINDING_CURSE.registryKey()).getOrThrow(Enchantments.BINDING_CURSE), 1);
            maxProcessor.getPlayer().getInventory().setItem(EquipmentSlot.HEAD.getIndex(36), itemStack);
        }

        var gameSession = maxProcessor.getGameSession();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (ProcessorManager.get(player).getGameSession().equals(gameSession)) {
                ProcessorManager.get(player).setCurrentGame(ModGames.NULL);
                player.setRespawnPosition(null, false);
                player.setGameMode(GameType.ADVENTURE);
                Util.teleportTo(player, level.getRespawnData().pos().getBottomCenter());
            }
        }
        gameSession.gameEnd();

        var pos = level.getRespawnData().pos().getBottomCenter();
        var firework = new ItemStack(Items.FIREWORK_ROCKET);
        firework.set(DataComponents.FIREWORKS, new Fireworks(0, List.of(new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, IntList.of(DyeColor.RED.getFireworkColor()), IntList.of(), true, true))));
        level.addFreshEntity(new FireworkRocketEntity(level, pos.x(), pos.y() + 3, pos.z(), firework));
        return false;
    }).wait(5).action((context, handler) -> {
        var level = context.processors().getFirst().getPlayer().level();
        var pos = level.getRespawnData().pos().getBottomCenter();
        var firework = new ItemStack(Items.FIREWORK_ROCKET);
        firework.set(DataComponents.FIREWORKS, new Fireworks(0, List.of(new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, IntList.of(DyeColor.GREEN.getFireworkColor()), IntList.of(), true, true))));
        level.addFreshEntity(new FireworkRocketEntity(level, pos.x(), pos.y() + 3, pos.z(), firework));
        return false;
    }).wait(5).action((context, handler) -> {
        var level = context.processors().getFirst().getPlayer().level();
        var pos = level.getRespawnData().pos().getBottomCenter();
        var firework = new ItemStack(Items.FIREWORK_ROCKET);
        firework.set(DataComponents.FIREWORKS, new Fireworks(0, List.of(new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, IntList.of(DyeColor.BLUE.getFireworkColor()), IntList.of(), true, true))));
        level.addFreshEntity(new FireworkRocketEntity(level, pos.x(), pos.y() + 3, pos.z(), firework));
        return true;
    }).build();

    private static boolean countdown(ServerActionContext context, String key) {
        for (var processor : context.processors()) {
            if (processor.emitEventToFirstMatch(processor.getGameSession().getGameData().onReadyEvent())) {
                for (var anotherProcessor : context.processors()) {
                    anotherProcessor.getPlayer().connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game.cancel")));
                }
                return true;
            }
        }
        showCountdown(context, key);
        return false;
    }

    private static void showCountdown(ServerActionContext context, String key) {
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(key)));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
    }

    public static final ActionSupplier<ServerActionContext> GAME_COUNTDOWN = ActionList.<ServerActionContext>getBuilder(
    ).action((context, handler) -> {
                showCountdown(context, "title.wuhumc.game.countdown_5");
                return false;
            }
    ).wait(20).action((context, handler) -> countdown(context, "title.wuhumc.game.countdown_4")
    ).wait(20).action((context, handler) -> countdown(context, "title.wuhumc.game.countdown_3")
    ).wait(20).action((context, handler) -> countdown(context, "title.wuhumc.game.countdown_2")
    ).wait(20).action((context, handler) -> countdown(context, "title.wuhumc.game.countdown_1")
    ).wait(20).action((context, handler) -> countdown(context, "title.wuhumc.game.countdown_end")
    ).action((context, handler) -> {
                context.processors().forEach(processor -> processor.getPlayer().getInventory().removeItemNoUpdate(0));
                ProcessorManager.getServerProcessor().planToRemoveActions(Util.handlerIsAction(getCountdownAction()));
                context.processors().forEach(processor -> {
                    processor.getInventoryCacheStack().push(new InventorySnapshot(processor.getPlayer()));
                    Util.removeInventoryItemsFromPlayer(processor.getPlayer());
                });
                context.processors().getFirst().getGameSession().gameStart();
                ProcessorManager.getServerProcessor().emitActions(context, context.processors().getFirst().getGameSession().getGameData().gameStartAction());
                return true;
            }
    ).build();

    private static ActionSupplier<ServerActionContext> getCountdownAction() {
        return GAME_COUNTDOWN;
    }

    public static void register() {
    }
}
