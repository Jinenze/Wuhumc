package xyz.jinenze.wuhumc.game;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.*;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.impl.WSNZSubGame;
import xyz.jinenze.wuhumc.init.ModEventListeners;
import xyz.jinenze.wuhumc.init.ModGames;
import xyz.jinenze.wuhumc.init.ModServerEvents;
import xyz.jinenze.wuhumc.util.PlayerItemUtil;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static xyz.jinenze.wuhumc.init.ModServerActions.*;

public class WSNZGame extends Game {
    static {
        var itemStack = new ItemStack(Items.DIAMOND);
        itemStack.applyComponents(DataComponentMap.builder().set(DataComponents.CUSTOM_NAME, Component.literal("friendship")).build());
        DIAMOND = itemStack;
    }

    private static final ItemStack DIAMOND;
    private final ArrayList<WSNZSubGame> subActionList = SubGames.getElements();
    private Iterator<WSNZSubGame> iterator;
    private WSNZSubGame currentGame;
    private int remainPoints;
    private int remainGames;

    public WSNZGame() {
        super(ModServerEvents.PLAYER_WSNZ_READY, WSNZ_MAIN, ModEventListeners.PLAYER_WSNZ_READY_PLAYER_NOT_READY);
    }

    public boolean hasNext() {
        return iterator.hasNext() && remainGames != 0;
    }

    public WSNZSubGame next(ServerActionContext context) {
        currentGame = iterator.next();
        remainPoints = currentGame.scoringRule().getTotalPoints(context);
        --remainGames;
        return currentGame;
    }

    public WSNZSubGame getCurrentGame() {
        return currentGame;
    }

    @Override
    public void addScore(ServerPlayer player) {
        if (remainPoints == 0) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_add_score_failed")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            return;
        }
        if (currentGame.score() > 0) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_add_score")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        } else {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_minus_score")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        ProcessorManager.get(player).addScore(currentGame.score());
        --remainPoints;
    }

    @Override
    public void gameStart() {
        super.gameStart();
        this.remainGames = Wuhumc.config.game_settings_wsnz.max_games;
        reroll();
    }

    public static ItemStack getDiamond() {
        return DIAMOND.copy();
    }

    public void reroll() {
        Collections.shuffle(subActionList);
        iterator = subActionList.iterator();
    }

    private static WSNZSubGame needMonitoringSubGame(int delay, int points, WSNZSubGame.ScoringRule scoringRule, Component text, Function<ServerPlayer, Boolean> monitor) {
        return new WSNZSubGame(delay, points, scoringRule, ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
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
            showPlayersScoreBoard(context);
            return true;
        }).build());
    }

    private static WSNZSubGame craftItemSubGame(int delay, int points, WSNZSubGame.ScoringRule scoringRule, Component text, EventListener<ServerPlayer> listener, List<ItemStack> requireItemStacks, List<Item> deleteItems) {
        return new WSNZSubGame(delay, points, scoringRule, ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
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
            showPlayersScoreBoard(context);
            return false;
        }).wait(4).action((context, handler) -> {
            for (var processor : context.processors()) {
                PlayerItemUtil.removeItemsFromPlayer(processor.getPlayer(), deleteItems);
                processor.removeListener(listener);
                resetPlayerPosition(processor.getPlayer());
            }
            PlayerItemUtil.deleteItenListFromGround(context.processors().getFirst().getPlayer().level(), deleteItems);
            return true;
        }).build());
    }

    private static WSNZSubGame needItemSubGame(int delay, int points, WSNZSubGame.ScoringRule scoringRule, Component text, EventListener<ServerPlayer> listener, List<Supplier<ItemStack>> requireItemStacks, List<Item> deleteItems) {
        return new WSNZSubGame(delay, points, scoringRule, ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
            for (var processor : context.processors()) {
                var player = processor.getPlayer();
                for (var supplier : requireItemStacks) {
                    player.addItem(supplier.get());
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
            PlayerItemUtil.deleteItenListFromGround(context.processors().getFirst().getPlayer().level(), deleteItems);
            showPlayersScoreBoard(context);
            return true;
        }).build());
    }

    private static WSNZSubGame basicSubGame(int delay, int points, WSNZSubGame.ScoringRule scoringRule, Component text, EventListener<ServerPlayer> listener) {
        return new WSNZSubGame(delay, points, scoringRule, ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
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
            showPlayersScoreBoard(context);
            return true;
        }).build());
    }

    public enum SubGames {
        WSNZ_6_NOT_ABOVE(needMonitoringSubGame(60, -1, WSNZSubGame.ScoringRule.INFINITE, Component.translatable("title.wuhumc.game_wsnz_6"), player -> player.getXRot() < -80f)),
        WSNZ_5_ABOVE(needMonitoringSubGame(60, 1, WSNZSubGame.ScoringRule.INFINITE, Component.translatable("title.wuhumc.game_wsnz_5"), player -> player.getXRot() < -80f)),
        WSNZ_4_CRAFT_AXE(craftItemSubGame(120, 1, WSNZSubGame.ScoringRule.INFINITE, Component.translatable("title.wuhumc.game_wsnz_4"), ModEventListeners.PLAYER_CRAFTED_DIAMOND_AXE_WSNZ_4, List.of(new ItemStack(Items.DIAMOND, 3), new ItemStack(Items.STICK, 2)), List.of(Items.DIAMOND, Items.STICK, Items.DIAMOND_AXE))),
        WSNZ_3_DIAMOND(needItemSubGame(120, 1, WSNZSubGame.ScoringRule.INFINITE, Component.translatable("title.wuhumc.game_wsnz_3"), ModEventListeners.PLAYER_ANOTHER_PLAYER_PICKUP_DIAMOND_WSNZ_3, List.of(WSNZGame::getDiamond), List.of(Items.DIAMOND))),
        WSNZ_2_SHIFT_DOWN(basicSubGame(60, 1, WSNZSubGame.ScoringRule.TOP_HALF, Component.translatable("title.wuhumc.game_wsnz_2"), ModEventListeners.PLAYER_SHIFT_DOWN_WSNZ_2)),
        WSNZ_1_FALL_VOID(basicSubGame(60, 1, WSNZSubGame.ScoringRule.TOP_HALF, Component.translatable("title.wuhumc.game_wsnz_1"), ModEventListeners.PLAYER_FALL_VOID_WSNZ_1)),
        ;
        private final WSNZSubGame game;

        SubGames(WSNZSubGame game) {
            this.game = game;
        }

        private static ArrayList<WSNZSubGame> getElements() {
            ArrayList<WSNZSubGame> result = new ArrayList<>();
            Arrays.stream(values()).toList().forEach(subGames -> result.add(subGames.game));
            return result;
        }
    }


    public static final ActionList<ServerActionContext> WSMZ_REFRESH = ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
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

    public static final ActionList<ServerActionContext> WSNZ_MAIN = ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
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
}
