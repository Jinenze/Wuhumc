package xyz.jinenze.wuhumc.init;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.*;
import xyz.jinenze.wuhumc.action.impl.WSNZActions;
import xyz.jinenze.wuhumc.config.ServerConfig;
import xyz.jinenze.wuhumc.game.WSNZGame;
import xyz.jinenze.wuhumc.util.PlayerItemUtil;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModServerActions {
    public static final Actions<ServerPlayer> NULL_PLAYER = Actions.<ServerPlayer>getBuilder().action((player, handler) -> true).build();
    public static final Actions<ServerActionContext> NULL_WORLD = Actions.<ServerActionContext>getBuilder().action((player, handler) -> true).build();

    public static final Actions<ServerPlayer> test = Actions.<ServerPlayer>getBuilder().action((player, handler) -> {
        player.level().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), "/me LLLLLL");
        return true;
    }).build();

    public static final ActionProvider<ServerPlayer> dumbActions = () -> (HasNextIterator<Action<ServerPlayer>>) () -> (player, handler) -> {
        player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1f, 0.5f);
        player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1f, 0.5f, 9));
        handler.setDelay(1);
        return false;
    };

    private static ActionProvider<ServerActionContext> showPlayerCounterDown(int delay) {
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

    private static ActionProvider<ServerPlayer> newPlayerMonitor(int delay, Function<ServerPlayer, Boolean> monitor) {
        return () -> new HasNextIterator<>() {
            private int lifeTime = delay;
            private final Action<ServerPlayer> action = (player, handler) -> {
                if (monitor.apply(player)) {
                    ModEventListeners.addScoreAndShowMessage(player);
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

    private static void resetPlayerPosition(ServerPlayer player) {
        var pos = player.getRespawnConfig().respawnData().pos().getBottomCenter();
        player.teleportTo(pos.x(), pos.y(), pos.z());
    }

    private static void ejectPlayer(ServerPlayer player) {
        resetPlayerPosition(player);
        player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), new Vec3(ProcessorManager.get(player).getCurrentGame().gameStartPlayerEjectDirection ? 1 : -1, 0.5, 0)));
        ProcessorManager.get(player).getCurrentGame().gameStartPlayerEjectDirection = !ProcessorManager.get(player).getCurrentGame().gameStartPlayerEjectDirection;
    }

    private static void setSpawnPoint(ServerPlayer player, ServerConfig.GamePosition config) {
        player.setRespawnPosition(new ServerPlayer.RespawnConfig(new LevelData.RespawnData(new GlobalPos(Level.OVERWORLD, new BlockPos(config.x, config.y, config.z)), 0, 0), true), false);
    }

    public static final Actions<ServerPlayer> RESPAWN_FLY = Actions.<ServerPlayer>getBuilder().action((player, handler) -> {
        if (Wuhumc.config.respawn_fly_enabled) {
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

        maxProcessor.getCurrentGame().gameEnd();

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (ProcessorManager.get(player).getCurrentGame().equals(maxProcessor.getCurrentGame())) {
                ProcessorManager.get(player).setCurrentGame(ModGames.NULL);
                player.setRespawnPosition(null, false);
                player.setGameMode(GameType.ADVENTURE);
                var pos = level.getRespawnData().pos().getBottomCenter();
                player.teleportTo(pos.x(), pos.y(), pos.z());
            }
        }

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

    private static WSNZActions needMonitoringWSNZAction(int delay, int points, WSNZActions.ScoringRule scoringRule, Component text, Function<ServerPlayer, Boolean> monitor) {
        return new WSNZActions(delay, points, scoringRule, Actions.<ServerActionContext>getBuilder().action((context, handler) -> {
            var action = newPlayerMonitor(delay, monitor);
            for (var processor : context.processors()) {
                processor.emitActions(action);
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSetTitleTextPacket(text));
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
                player.sendSystemMessage(scoringRule.getMessage());
            }
            return false;
        }).wait(delay).action((context, handler) -> {
            for (var processor : context.processors()) {
                resetPlayerPosition(processor.getPlayer());
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            }
            return true;
        }).build());
    }

    private static WSNZActions craftItemWSNZAction(int delay, int points, WSNZActions.ScoringRule scoringRule, Component text, EventListener<ServerPlayer> listener, List<ItemStack> requireItemStacks, List<Item> deleteItems) {
        return new WSNZActions(delay, points, scoringRule, Actions.<ServerActionContext>getBuilder().action((context, handler) -> {
            var blockPos = context.processors().getFirst().getPlayer().getRespawnConfig().respawnData().pos().above(3);
            context.processors().getFirst().getPlayer().level().setBlock(blockPos, Blocks.CRAFTING_TABLE.defaultBlockState(), 0);
            for (var processor : context.processors()) {
                var player = processor.getPlayer();
                for (var itemStack : requireItemStacks) {
                    player.addItem(itemStack);
                }
                player.connection.send(new ClientboundSetTitleTextPacket(text));
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
                player.connection.send(new ClientboundBlockUpdatePacket(blockPos, Blocks.CRAFTING_TABLE.defaultBlockState()));
                processor.emitListener(listener);
                player.sendSystemMessage(scoringRule.getMessage());
            }
            return false;
        }).wait(delay).action((context, handler) -> {
            var blockPos = context.processors().getFirst().getPlayer().getRespawnConfig().respawnData().pos().above(3);
            context.processors().getFirst().getPlayer().level().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 0);
            for (var processor : context.processors()) {
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
                player.connection.send(new ClientboundBlockUpdatePacket(blockPos, Blocks.AIR.defaultBlockState()));
            }
            return false;
        }).wait(4).action((context, handler) -> {
            for (var processor : context.processors()) {
                PlayerItemUtil.removeItemsFromPlayer(processor.getPlayer(), deleteItems);
                processor.removeListener(listener);
                resetPlayerPosition(processor.getPlayer());
            }
            return true;
        }).build());
    }

    private static WSNZActions needItemWSNZAction(int delay, int points, WSNZActions.ScoringRule scoringRule, Component text, EventListener<ServerPlayer> listener, List<ItemStack> requireItemStacks, List<Item> deleteItems) {
        return new WSNZActions(delay, points, scoringRule, Actions.<ServerActionContext>getBuilder().action((context, handler) -> {
            for (var processor : context.processors()) {
                var player = processor.getPlayer();
                for (var itemStack : requireItemStacks) {
                    player.addItem(itemStack);
                }
                player.connection.send(new ClientboundSetTitleTextPacket(text));
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
                processor.emitListener(listener);
                player.sendSystemMessage(scoringRule.getMessage());
            }
            return false;
        }).wait(delay).action((context, handler) -> {
            for (var processor : context.processors()) {
                PlayerItemUtil.removeItemsFromPlayer(processor.getPlayer(), deleteItems);
                processor.removeListener(listener);
                resetPlayerPosition(processor.getPlayer());
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            }
            return true;
        }).build());
    }

    private static WSNZActions basicWSNZAction(int delay, int points, WSNZActions.ScoringRule scoringRule, Component text, EventListener<ServerPlayer> listener) {
        return new WSNZActions(delay, points, scoringRule, Actions.<ServerActionContext>getBuilder().action((context, handler) -> {
            for (var processor : context.processors()) {
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSetTitleTextPacket(text));
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
                processor.emitListener(listener);
                player.sendSystemMessage(scoringRule.getMessage());
            }
            return false;
        }).wait(delay).action((context, handler) -> {
            for (var processor : context.processors()) {
                processor.removeListener(listener);
                resetPlayerPosition(processor.getPlayer());
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            }
            return true;
        }).build());
    }

    public static final WSNZActions WSNZ_6_NOT_ABOVE = needMonitoringWSNZAction(60, -1, WSNZActions.ScoringRule.INFINITE, Component.translatable("title.wuhumc.game_wsnz_6"), player -> player.getXRot() < -80f);

    public static final WSNZActions WSNZ_5_ABOVE = needMonitoringWSNZAction(60, 1, WSNZActions.ScoringRule.INFINITE, Component.translatable("title.wuhumc.game_wsnz_5"), player -> player.getXRot() < -80f);

    public static final WSNZActions WSNZ_4_CRAFT_AXE = craftItemWSNZAction(120, 1, WSNZActions.ScoringRule.INFINITE, Component.translatable("title.wuhumc.game_wsnz_4"), ModEventListeners.PLAYER_CRAFTED_DIAMOND_AXE_WSNZ_4, List.of(new ItemStack(Items.DIAMOND, 3), new ItemStack(Items.STICK, 2)), List.of(Items.DIAMOND, Items.STICK, Items.DIAMOND_AXE));

    public static final WSNZActions WSNZ_3_DIAMOND = needItemWSNZAction(120, 1, WSNZActions.ScoringRule.INFINITE, Component.translatable("title.wuhumc.game_wsnz_3"), ModEventListeners.PLAYER_ANOTHER_PLAYER_PICKUP_DIAMOND_WSNZ_3, List.of(WSNZGame.getDiamond()), List.of(Items.DIAMOND));

    public static final WSNZActions WSNZ_2_SHIFT_DOWN = basicWSNZAction(60, 1, WSNZActions.ScoringRule.TOP_HALF, Component.translatable("title.wuhumc.game_wsnz_2"), ModEventListeners.PLAYER_SHIFT_DOWN_WSNZ_2);

    public static final WSNZActions WSNZ_1_FALL_VOID = basicWSNZAction(60, 1, WSNZActions.ScoringRule.TOP_HALF, Component.translatable("title.wuhumc.game_wsnz_1"), ModEventListeners.PLAYER_FALL_VOID_WSNZ_1);

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
        var action = ModGames.WSNZ.getCurrentGame();
        ProcessorManager.getServerProcessor().emitActions(context, action);
        ProcessorManager.getServerProcessor().emitActions(context, showPlayerCounterDown(action.delay()));
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
                if (ModGames.WSNZ.hasNext()) {
                    var action = ModGames.WSNZ.next(context);
                    ProcessorManager.getServerProcessor().emitActions(context, WSMZ_REFRESH);
                    handler.setDelay(action.delay() + 50 + 50);
                    return false;
                }
                ProcessorManager.getServerProcessor().emitActions(context, GAME_END);
                return true;
            };
        }
    };

    public static final Actions<ServerActionContext> WSNZ_MAIN = Actions.<ServerActionContext>getBuilder().action((context, handler) -> {
        for (var processor : context.processors()) {
            ModGames.WSNZ.gameStart();
            setSpawnPoint(processor.getPlayer(), Wuhumc.config.game_settings_wsnz.game_position_wsnz);
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
