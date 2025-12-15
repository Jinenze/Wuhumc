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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.*;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.impl.WSNZSubGameData;
import xyz.jinenze.wuhumc.init.ModEventListeners;
import xyz.jinenze.wuhumc.init.ModGames;
import xyz.jinenze.wuhumc.init.ModServerEvents;
import xyz.jinenze.wuhumc.util.PlayerUtil;

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
    private static final ArrayList<WSNZSubGameData> stageOneSubGames = getSubGames(StageOneSubGameImpl.values());
    private static final ArrayList<WSNZSubGameData> stageTwoSubGames = getSubGames(StageTwoSubGameImpl.values());
    private static final ArrayList<WSNZSubGameData> stageThreeSubGames = getSubGames(StageThreeSubGameImpl.values());
    private static final List<ArrayList<WSNZSubGameData>> stages = List.of(stageOneSubGames, stageTwoSubGames, stageThreeSubGames);
    private Iterator<ArrayList<WSNZSubGameData>> currentStages;
    private Iterator<WSNZSubGameData> currentSubGames;
    private WSNZSubGameData currentSubGame;
    private ScoreProcessor currrentScoreProcessor;
    private int remainGames;

    public WSNZGame() {
        super(ModServerEvents.PLAYER_WSNZ_READY, WSNZ_MAIN, ModEventListeners.PLAYER_WSNZ_READY_PLAYER_NOT_READY);
    }

    @Override
    public void gameStart() {
        super.gameStart();
        remainGames = Wuhumc.config.game_settings_wsnz.max_games;
        currentStages = stages.iterator();
        stages.forEach(Collections::shuffle);
        currentSubGames = currentStages.next().iterator();
    }

    public WSNZSubGameData next(ServerActionContext context) {
        if (currentSubGames.hasNext() && remainGames > 0) {
            currentSubGame = currentSubGames.next();
        } else {
            currentSubGames = currentStages.next().iterator();
            currentSubGame = currentSubGames.next();
            remainGames = Wuhumc.config.game_settings_wsnz.max_games;
        }
        --remainGames;
        currrentScoreProcessor = new ScoreProcessor(currentSubGame, context);
        return currentSubGame;
    }

    public boolean hasNext() {
        return (currentSubGames.hasNext() && remainGames > 0) || currentStages.hasNext();
    }

    @Override
    public void addScore(ServerPlayer player) {
        int score = currrentScoreProcessor.getNextScore();
        if (score == 0) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_add_score_failed")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            return;
        } else if (score > 0) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_add_score")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        } else {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_minus_score")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        ProcessorManager.get(player).addCurrentScore(score);
    }

    public WSNZSubGameData getCurrentSubGame() {
        return currentSubGame;
    }

    private static class ScoreProcessor {
        private int sorerCount;
        private final ServerActionContext context;
        private final WSNZSubGameData game;

        private int getNextScore() {
            ++sorerCount;
            return game.scoringRule().getScore(sorerCount, game.scoreFactor(), context);
        }

        public ScoreProcessor(WSNZSubGameData game, ServerActionContext context) {
            this.game = game;
            this.context = context;
        }
    }

    private static WSNZSubGameData needMonitorSubGame(int delay, int score, WSNZSubGameData.ScoringRule scoringRule, Component text, Function<ServerPlayer, Boolean> monitor) {
        return new WSNZSubGameData(delay, score, scoringRule, ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
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
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            }
            showPlayersScoreBoard(context);
            return true;
        }).build());
    }

    private static WSNZSubGameData craftItemSubGame(int delay, int score, WSNZSubGameData.ScoringRule scoringRule, Component text, EventListener<ServerPlayer> listener, List<Supplier<ItemStack>> requireItemStacks) {
        return new WSNZSubGameData(delay, score, scoringRule, ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
            var blockPos = context.processors().getFirst().getPlayer().getRespawnConfig().respawnData().pos().above(3);
            context.processors().getFirst().getPlayer().level().setBlock(blockPos, Blocks.CRAFTING_TABLE.defaultBlockState(), 0);
            for (var processor : context.processors()) {
                var player = processor.getPlayer();
                for (var supplier : requireItemStacks) {
                    player.addItem(supplier.get());
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
                player.getInventory().clearContent();
                processor.removeListener(listener);
            }
            showPlayersScoreBoard(context);
            PlayerUtil.removeItemsFromGround(context.processors().getFirst().getPlayer().level());
            return true;
        }).build());
    }

    private static WSNZSubGameData needItemSubGame(int delay, int score, WSNZSubGameData.ScoringRule scoringRule, Component text, EventListener<ServerPlayer> listener, List<Supplier<ItemStack>> requireItemStacks) {
        return new WSNZSubGameData(delay, score, scoringRule, ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
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
                processor.getPlayer().getInventory().clearContent();
                processor.removeListener(listener);
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            }
            PlayerUtil.removeItemsFromGround(context.processors().getFirst().getPlayer().level());
            showPlayersScoreBoard(context);
            return true;
        }).build());
    }

    private static WSNZSubGameData basicSubGame(int delay, int score, WSNZSubGameData.ScoringRule scoringRule, Component text, EventListener<ServerPlayer> listener) {
        return new WSNZSubGameData(delay, score, scoringRule, ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
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
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            }
            showPlayersScoreBoard(context);
            return true;
        }).build());
    }

    private static ArrayList<WSNZSubGameData> getSubGames(SubGameImpl[] values) {
        ArrayList<WSNZSubGameData> result = new ArrayList<>();
        Arrays.stream(values).toList().forEach(subGames -> result.add(subGames.getGame()));
        return result;
    }

    private interface SubGameImpl {
        WSNZSubGameData getGame();
    }

    private enum StageOneSubGameImpl implements SubGameImpl {
        NOT_BElOW(needMonitorSubGame(40, -1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_1_not_below"), player -> player.getXRot() > 80f)),
        BELOW(needMonitorSubGame(40, 1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_1_below"), player -> player.getXRot() > 80f)),
        NOT_ABOVE(needMonitorSubGame(40, -1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_1_not_above"), player -> player.getXRot() < -80f)),
        ABOVE(needMonitorSubGame(40, 1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_1_above"), player -> player.getXRot() < -80f)),
        CRAFT_AXE(craftItemSubGame(120, 1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_1_craft_axe"), ModEventListeners.PLAYER_CRAFTED_DIAMOND_AXE, List.of(() -> new ItemStack(Items.DIAMOND, 3), () -> new ItemStack(Items.STICK, 2)))),
        DIAMOND_GIFT(needItemSubGame(120, 1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_1_diamond_gift"), ModEventListeners.PLAYER_ANOTHER_PLAYER_PICKUP_DIAMOND, List.of(DIAMOND::copy))),
        SNEAK(basicSubGame(60, 1, WSNZSubGameData.ScoringRuleImpl.TOP_HALF, Component.translatable("title.wuhumc.game_wsnz_1_sneak"), ModEventListeners.PLAYER_SHIFT_DOWN)),
        FALL_VOID(basicSubGame(60, 1, WSNZSubGameData.ScoringRuleImpl.TOP_HALF, Component.translatable("title.wuhumc.game_wsnz_1_fall_void"), ModEventListeners.PLAYER_FALL_VOID)),
        ;
        private final WSNZSubGameData game;

        @Override
        public WSNZSubGameData getGame() {
            return game;
        }

        StageOneSubGameImpl(WSNZSubGameData game) {
            this.game = game;
        }
    }

    private enum StageTwoSubGameImpl implements SubGameImpl {
        NOT_BElOW(needMonitorSubGame(40, -1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_1_not_below"), player -> player.getXRot() > 80f)),
        ;
        private final WSNZSubGameData game;

        @Override
        public WSNZSubGameData getGame() {
            return game;
        }

        StageTwoSubGameImpl(WSNZSubGameData game) {
            this.game = game;
        }
    }

    private enum StageThreeSubGameImpl implements SubGameImpl {
        NOT_BElOW(needMonitorSubGame(40, -1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_1_not_below"), player -> player.getXRot() > 80f)),
        ;
        private final WSNZSubGameData game;

        @Override
        public WSNZSubGameData getGame() {
            return game;
        }

        StageThreeSubGameImpl(WSNZSubGameData game) {
            this.game = game;
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
        var game = ((WSNZGame) context.processors().getFirst().getCurrentGame()).getCurrentSubGame();
        ProcessorManager.getServerProcessor().emitActions(context, showPlayerCounterDown(game.totalTime()));
        return true;
    }).build();

    public static final ActionProvider<ServerActionContext> WSNZ_LOOP = () -> new HasNextIterator<>() {
        private final Queue<ActionsHandler<ServerActionContext>> handlers = new LinkedList<>();

        @Override
        public Action<ServerActionContext> next() {
            return (context, handler) -> {
                if (handlers.isEmpty()) {
                    var game = (WSNZGame) context.processors().getFirst().getCurrentGame();
                    if (game.hasNext()) {
                        handlers.add(new ActionsHandler<>(context, WSMZ_REFRESH));
                        handlers.add(new ActionsHandler<>(context, game.next(context).actions()));
                    } else {
                        ProcessorManager.getServerProcessor().emitActions(context, GAME_END);
                        return true;
                    }
                }
                if (handlers.element().tick()) {
                    handlers.remove();
                }
                handler.setDelay(1);
                return false;
            };
        }
    };

    public static final ActionList<ServerActionContext> WSNZ_MAIN = ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
        for (var processor : context.processors()) {
            ModGames.WSNZ.gameStart();
            PlayerUtil.setSpawnPoint(processor.getPlayer(), Wuhumc.config.game_settings_wsnz.game_position_wsnz);
            PlayerUtil.ejectPlayer(processor.getPlayer());
        }
        return false;
    }).wait(20).action(((context, handler) -> {
        ProcessorManager.getServerProcessor().emitActions(context, WSNZ_LOOP);
        return true;
    })).build();
}
