package xyz.jinenze.wuhumc.game;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.*;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.init.ModEvents;
import xyz.jinenze.wuhumc.init.ModGames;
import xyz.jinenze.wuhumc.util.Util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static xyz.jinenze.wuhumc.init.ModServerActions.*;

public class WSNZGameSession extends GameSession implements HasNextIterator<Action<ServerActionContext>> {
    static {
        var itemStack = new ItemStack(Items.DIAMOND);
        itemStack.applyComponents(DataComponentMap.builder().set(DataComponents.CUSTOM_NAME, Component.literal("friendship")).build());
        DIAMOND = itemStack;
    }

    private static final ItemStack DIAMOND;

    private final Queue<ActionsHandler<ServerActionContext>> handlers;
    private final Iterator<ArrayList<WSNZSubGameData>> stagesIterator;
    private final Iterator<Supplier<Integer>> stageMaxRoundsIterator;
    private Iterator<WSNZSubGameData> subGamesIterator;
    private WSNZSubGameData currentSubGame;
    private int remainRounds;
    private ScoreProcessor currrentScoreProcessor;
    private final Action<ServerActionContext> action;

    public WSNZGameSession() {
        stages.forEach(Collections::shuffle);
        stagesIterator = stages.iterator();
        stageMaxRoundsIterator = stageMaxRounds.iterator();
        subGamesIterator = stagesIterator.next().iterator();
        remainRounds = stageMaxRoundsIterator.next().get();
        handlers = new LinkedList<>();
        action = (context, handler) -> {
            if (handlers.isEmpty()) {
                if (subGamesIterator.hasNext() && remainRounds > 0) {
                    currentSubGame = subGamesIterator.next();
                    handlers.add(new ActionsHandler<>(context, currentSubGame.actions()));
                    currrentScoreProcessor = new ScoreProcessor(currentSubGame, context);
                    --remainRounds;
                } else if (stagesIterator.hasNext()) {
                    subGamesIterator = stagesIterator.next().iterator();
                    remainRounds = stageMaxRoundsIterator.next().get();
                    return next().run(context, handler);
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

    @Override
    public Action<ServerActionContext> next() {
        return action;
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
        ProcessorManager.get(player).addScore(score);
    }

    @Override
    public void gameEnd() {
        super.gameEnd();
        ModGames.WSNZ = new WSNZGameSession();
    }

    @Override
    public GameData getGameData() {
        return GAME_DATA;
    }

    //todo: wtf
    private WSNZSubGameData getCurrentSubGame() {
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

    private static final ActionList.Builder<ServerActionContext> REFRESH_ACTIONS = ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_wsnz_refresh_1")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        return false;
    }).wait(5).action((context, handler) -> {
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_wsnz_refresh_2")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        return false;
    }).wait(5).action((context, handler) -> {
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_wsnz_refresh_3")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        return false;
    }).wait(20).action((context, handler) -> {
        //todo: we need refactor
        ProcessorManager.getServerProcessor().emitActions(context, displayCountdown(((WSNZGameSession) context.processors().getFirst().getGameSession()).getCurrentSubGame().totalTime()));
        return false;
    });

    private static final Map<ServerPlayerProcessor, Integer> DATA_IRON_ARMOR_PIECES_COUNT = new WeakHashMap<>();
    private static final WSNZSubGameData CRAFT_FULL_IRON_ARMOR = new WSNZSubGameData(160, 4, WSNZSubGameData.ScoringRuleImpl.TOP_DECREASE, REFRESH_ACTIONS.copy().action((context, handler) -> {
        var blockPos = context.processors().getFirst().getPlayer().getRespawnConfig().respawnData().pos().above(3);
        context.processors().getFirst().getPlayer().level().setBlock(blockPos, Blocks.CRAFTING_TABLE.defaultBlockState(), 0);
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.addItem(new ItemStack(Items.IRON_INGOT, 24));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_wsnz_craft_full_iron_armor")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            player.connection.send(new ClientboundBlockUpdatePacket(blockPos, Blocks.CRAFTING_TABLE.defaultBlockState()));
            DATA_IRON_ARMOR_PIECES_COUNT.put(ProcessorManager.get(player), 0);
            processor.emitListener(WSNZListeners.PLAYER_CRAFTED_IRON_HELMET);
            processor.emitListener(WSNZListeners.PLAYER_CRAFTED_IRON_CHESTPLATE);
            processor.emitListener(WSNZListeners.PLAYER_CRAFTED_IRON_LEGGING);
            processor.emitListener(WSNZListeners.PLAYER_CRAFTED_IRON_BOOTS);
            player.sendSystemMessage(WSNZSubGameData.ScoringRuleImpl.TOP_DECREASE.getMessage());
        }
        return false;
    }).wait(180).action((context, handler) -> {
        var blockPos = context.processors().getFirst().getPlayer().getRespawnConfig().respawnData().pos().above(3);
        context.processors().getFirst().getPlayer().level().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 0);
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            player.connection.send(new ClientboundBlockUpdatePacket(blockPos, Blocks.AIR.defaultBlockState()));
            Util.removeCurrentContainerItemsFromPlayer(player);
            processor.removeListener(WSNZListeners.PLAYER_CRAFTED_IRON_HELMET);
            processor.removeListener(WSNZListeners.PLAYER_CRAFTED_IRON_CHESTPLATE);
            processor.removeListener(WSNZListeners.PLAYER_CRAFTED_IRON_LEGGING);
            processor.removeListener(WSNZListeners.PLAYER_CRAFTED_IRON_BOOTS);
        }
        showPlayersScoreBoard(context);
        Util.removeItemsFromGround(context.processors().getFirst().getPlayer().level());
        return true;
    }).build());

    private static final Map<ActionsHandler<ServerActionContext>, List<Horse>> DATA_HORSES = new WeakHashMap<>();
    private static final WSNZSubGameData HORSE_DUEL = new WSNZSubGameData(300, 1, WSNZSubGameData.ScoringRuleImpl.INFINITE, REFRESH_ACTIONS.copy().action((context, handler) -> {
        List<Horse> horses = new ArrayList<>();
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            var horse = new Horse(EntityType.HORSE, player.level());
            horse.setPos(player.position());
            horse.tameWithName(player);
            horse.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
            horses.add(horse);
            player.level().addFreshEntity(horse);
            player.startRiding(horse, true, false);
            player.addItem(new ItemStack(Items.NETHERITE_SPEAR, 1));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_wsnz_horse_duel")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            processor.emitListener(WSNZListeners.PLAYER_KILLED_ANOTHER_PLAYER);
            player.sendSystemMessage(WSNZSubGameData.ScoringRuleImpl.INFINITE.getMessage());
        }
        DATA_HORSES.put(handler, horses);
        return false;
    }).wait(300).action((context, handler) -> {
        for (var processor : context.processors()) {
            var player = processor.getPlayer();
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            Util.removeCurrentContainerItemsFromPlayer(processor.getPlayer());
            processor.removeListener(WSNZListeners.PLAYER_KILLED_ANOTHER_PLAYER);
        }
        DATA_HORSES.get(handler).forEach(Entity::discard);
        Util.removeItemsFromGround(context.processors().getFirst().getPlayer().level());
        showPlayersScoreBoard(context);
        return true;
    }).build());

    private static final ArrayList<WSNZSubGameData> stageOneSubGames = getSubGames(StageOneSubGameImpl.values());
    private static final ArrayList<WSNZSubGameData> stageTwoSubGames = getSubGames(StageTwoSubGameImpl.values());
    private static final ArrayList<WSNZSubGameData> stageThreeSubGames = getSubGames(StageThreeSubGameImpl.values());
    private static final List<ArrayList<WSNZSubGameData>> stages = List.of(stageOneSubGames, stageTwoSubGames, stageThreeSubGames);
    private static final List<Supplier<Integer>> stageMaxRounds = List.of(() -> Wuhumc.config.game_settings_wsnz.stage_one_max_rounds, () -> Wuhumc.config.game_settings_wsnz.stage_two_max_rounds, () -> Wuhumc.config.game_settings_wsnz.stage_three_max_rounds);

    private static WSNZSubGameData needMonitorSubGame(int totalTime, int score, WSNZSubGameData.ScoringRule scoringRule, Component text, Function<ServerPlayer, Boolean> monitor) {
        return new WSNZSubGameData(totalTime, score, scoringRule, REFRESH_ACTIONS.copy().action((context, handler) -> {
            var action = newPlayerMonitor(totalTime, monitor);
            for (var processor : context.processors()) {
                processor.emitActions(action);
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSetTitleTextPacket(text));
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
                player.sendSystemMessage(scoringRule.getMessage());
            }
            return false;
        }).wait(totalTime).action((context, handler) -> {
            for (var processor : context.processors()) {
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            }
            showPlayersScoreBoard(context);
            return true;
        }).build());
    }

    private static WSNZSubGameData craftItemSubGame(int totalTime, int score, WSNZSubGameData.ScoringRule scoringRule, Component text, EventListener<ServerPlayer> listener, List<Supplier<ItemStack>> requireItemStacks) {
        return new WSNZSubGameData(totalTime, score, scoringRule, REFRESH_ACTIONS.copy().action((context, handler) -> {
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
        }).wait(totalTime).action((context, handler) -> {
            var blockPos = context.processors().getFirst().getPlayer().getRespawnConfig().respawnData().pos().above(3);
            context.processors().getFirst().getPlayer().level().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 0);
            for (var processor : context.processors()) {
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
                player.connection.send(new ClientboundBlockUpdatePacket(blockPos, Blocks.AIR.defaultBlockState()));
                Util.removeCurrentContainerItemsFromPlayer(player);
                processor.removeListener(listener);
            }
            showPlayersScoreBoard(context);
            Util.removeItemsFromGround(context.processors().getFirst().getPlayer().level());
            return true;
        }).build());
    }

    private static WSNZSubGameData needItemSubGame(int totalTime, int score, WSNZSubGameData.ScoringRule scoringRule, Component text, EventListener<ServerPlayer> listener, List<Supplier<ItemStack>> requireItemStacks) {
        return new WSNZSubGameData(totalTime, score, scoringRule, REFRESH_ACTIONS.copy().action((context, handler) -> {
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
        }).wait(totalTime).action((context, handler) -> {
            for (var processor : context.processors()) {
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
                Util.removeCurrentContainerItemsFromPlayer(processor.getPlayer());
                processor.removeListener(listener);
            }
            Util.removeItemsFromGround(context.processors().getFirst().getPlayer().level());
            showPlayersScoreBoard(context);
            return true;
        }).build());
    }

    private static WSNZSubGameData basicSubGame(int totalTime, int score, WSNZSubGameData.ScoringRule scoringRule, Component text, EventListener<ServerPlayer> listener) {
        return new WSNZSubGameData(totalTime, score, scoringRule, REFRESH_ACTIONS.copy().action((context, handler) -> {
            for (var processor : context.processors()) {
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSetTitleTextPacket(text));
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
                processor.emitListener(listener);
                player.sendSystemMessage(scoringRule.getMessage());
            }
            return false;
        }).wait(totalTime).action((context, handler) -> {
            for (var processor : context.processors()) {
                processor.removeListener(listener);
                var player = processor.getPlayer();
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            }
            showPlayersScoreBoard(context);
            return true;
        }).build());
    }

    private static final ActionSupplier<ServerActionContext> WSNZ_LOOP = () -> ModGames.WSNZ;

    public static final ActionList<ServerActionContext> WSNZ_MAIN = ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
        context.processors().getFirst().getGameSession().gameStart();
        for (var processor : context.processors()) {
            Util.setOverworldSpawnPoint(processor.getPlayer(), Wuhumc.config.game_settings_wsnz.position);
            Util.resetPlayerPosition(processor.getPlayer());
        }
        return false;
    }).wait(20).action(((context, handler) -> {
        ProcessorManager.getServerProcessor().emitActions(context, WSNZ_LOOP);
        return true;
    })).build();

    private interface SubGameImpl {
        WSNZSubGameData getGame();
    }

    private enum StageOneSubGameImpl implements SubGameImpl {
        NOT_BElOW(needMonitorSubGame(20, -1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_not_below"), player -> player.getXRot() > 80f)),
        BELOW(needMonitorSubGame(20, 1, WSNZSubGameData.ScoringRuleImpl.TOP_HALF, Component.translatable("title.wuhumc.game_wsnz_below"), player -> player.getXRot() > 80f)),
        NOT_ABOVE(needMonitorSubGame(20, -1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_not_above"), player -> player.getXRot() < -80f)),
        ABOVE(needMonitorSubGame(20, 1, WSNZSubGameData.ScoringRuleImpl.TOP_HALF, Component.translatable("title.wuhumc.game_wsnz_above"), player -> player.getXRot() < -80f)),
        NOT_SNEAK(basicSubGame(20, -1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_sneak"), WSNZListeners.PLAYER_SNEAK)),
        SNEAK(basicSubGame(20, 1, WSNZSubGameData.ScoringRuleImpl.TOP_HALF, Component.translatable("title.wuhumc.game_wsnz_sneak"), WSNZListeners.PLAYER_SNEAK)),
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
        FALL_VOID(basicSubGame(60, 1, WSNZSubGameData.ScoringRuleImpl.TOP_HALF, Component.translatable("title.wuhumc.game_wsnz_fall_void"), WSNZListeners.PLAYER_FALL_VOID)),
        DIAMOND_GIFT(needItemSubGame(120, 1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_diamond_gift"), WSNZListeners.PLAYER_ANOTHER_PLAYER_PICKUP_DIAMOND, List.of(DIAMOND::copy))),
        PLAYER_DUEL(needItemSubGame(120, 1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_player_duel"), WSNZListeners.PLAYER_KILLED_ANOTHER_PLAYER, List.of(() -> new ItemStack(Items.NETHERITE_SPEAR)))),
        CRAFT_FULL_IRON_ARMOR(WSNZGameSession.CRAFT_FULL_IRON_ARMOR),
        CRAFT_DIAMOND_AXE(craftItemSubGame(120, 1, WSNZSubGameData.ScoringRuleImpl.INFINITE, Component.translatable("title.wuhumc.game_wsnz_craft_axe"), WSNZListeners.PLAYER_CRAFTED_DIAMOND_AXE, List.of(() -> new ItemStack(Items.DIAMOND, 3), () -> new ItemStack(Items.STICK, 2)))),
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
        HORSE_DUEL(WSNZGameSession.HORSE_DUEL),
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

    private static ArrayList<WSNZSubGameData> getSubGames(SubGameImpl[] values) {
        ArrayList<WSNZSubGameData> result = new ArrayList<>();
        Arrays.stream(values).toList().forEach(subGames -> result.add(subGames.getGame()));
        return result;
    }

    private static void ironArmorCheck(ServerPlayer player) {
        int count = DATA_IRON_ARMOR_PIECES_COUNT.get(ProcessorManager.get(player));
        if (count < 3) {
            DATA_IRON_ARMOR_PIECES_COUNT.put(ProcessorManager.get(player), count + 1);
        } else {
            Util.addScore(player);
        }
    }

    private enum WSNZListeners implements EventListener<ServerPlayer> {
        PLAYER_WSNZ_READY(ModEvents.PLAYER_WSNZ_READY, player -> ProcessorManager.get(player).emitListener(new Supplier<>() {
            @Override
            public EventListener<ServerPlayer> get() {
                return PLAYER_WSNZ_READY;
            }
        })),
        PLAYER_FALL_VOID(ModEvents.PLAYER_FALL_VOID, Util::addScore),
        PLAYER_SNEAK(ModEvents.PLAYER_SNEAK, Util::addScore),
        PLAYER_ANOTHER_PLAYER_PICKUP_DIAMOND(ModEvents.PLAYER_ANOTHER_PLAYER_PICKUP_DIAMOND, Util::addScore),
        PLAYER_CRAFTED_DIAMOND_AXE(new ModEvents.CraftEvent(ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("diamond_axe"))), Util::addScore),
        PLAYER_CRAFTED_IRON_HELMET(new ModEvents.CraftEvent(ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("iron_helmet"))), WSNZGameSession::ironArmorCheck),
        PLAYER_CRAFTED_IRON_CHESTPLATE(new ModEvents.CraftEvent(ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("iron_chestplate"))), WSNZGameSession::ironArmorCheck),
        PLAYER_CRAFTED_IRON_LEGGING(new ModEvents.CraftEvent(ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("iron_leggings"))), WSNZGameSession::ironArmorCheck),
        PLAYER_CRAFTED_IRON_BOOTS(new ModEvents.CraftEvent(ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("iron_boots"))), WSNZGameSession::ironArmorCheck),
        PLAYER_KILLED_ANOTHER_PLAYER(ModEvents.PLAYER_KILLED_ANOTHER_PLAYER, Util::addScore),
        ;

        private final Event event;
        private final Consumer<ServerPlayer> action;

        WSNZListeners(Event event, Consumer<ServerPlayer> action) {
            this.event = event;
            this.action = action;
        }

        @Override
        public Event getEvent() {
            return event;
        }

        @Override
        public Consumer<ServerPlayer> getAction() {
            return action;
        }
    }

    private record WSNZSubGameData(int totalTime, int scoreFactor, ScoringRule scoringRule,
                                   ActionSupplier<ServerActionContext> actions) {
        public interface ScoringRule extends ScoreGetter {
            Component getMessage();
        }

        public interface ScoreGetter {
            int getScore(int scorerCount, int scoreFactor, ServerActionContext context);
        }

        private enum ScoringRuleImpl implements ScoringRule {
            NONE((scorerCount, scoreFactor, context) -> 0, Component.literal("")),
            INFINITE((scorerCount, scoreFactor, context) -> scoreFactor, Component.translatable("message.wuhumc.game_wsnz_scoring_rule_infinite")),
            TOP_ONE((scorerCount, scoreFactor, context) -> scorerCount == 1 ? scoreFactor : 0, Component.translatable("message.wuhumc.game_wsnz_scoring_rule_top_one")),
            TOP_HALF((scorerCount, scoreFactor, context) -> scorerCount > context.processors().size() / 2 ? scoreFactor : 0, Component.translatable("message.wuhumc.game_wsnz_scoring_rule_top_half")),
            TOP_THREE((scorerCount, scoreFactor, context) -> scorerCount > 3 ? 0 : scoreFactor, Component.translatable("message.wuhumc.game_wsnz_scoring_rule_top_three")),
            TOP_DECREASE((scorerCount, scoreFactor, context) -> Math.max(scoreFactor - scorerCount, 0), Component.translatable("message.wuhumc.game_wsnz_scoring_rule_top_decrease")),
            ;

            private final ScoreGetter scoreGetter;
            private final Component message;

            ScoringRuleImpl(ScoreGetter scoreGetter, Component message) {
                this.scoreGetter = scoreGetter;
                this.message = message;
            }

            @Override
            public int getScore(int scorerCount, int scoreFactor, ServerActionContext context) {
                return scoreGetter.getScore(scorerCount, scoreFactor, context);
            }

            @Override
            public Component getMessage() {
                return message;
            }
        }
    }

    public static final GameData GAME_DATA = new GameData(ModEvents.PLAYER_WSNZ_READY, WSNZ_MAIN, WSNZListeners.PLAYER_WSNZ_READY);
}
