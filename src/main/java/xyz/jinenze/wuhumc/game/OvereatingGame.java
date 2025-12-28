package xyz.jinenze.wuhumc.game;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableInt;
import xyz.jinenze.wuhumc.action.*;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.init.ModEvents;
import xyz.jinenze.wuhumc.init.ModServerActions;
import xyz.jinenze.wuhumc.util.Util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OvereatingGame extends GameSession {
    private final GameData gameData;
    private int scorerCount = 0;

    public OvereatingGame(GameData gameData) {
        this.gameData = gameData;
    }

    @Override
    public GameData getGameData() {
        return gameData;
    }

    @Override
    public void addScore(ServerPlayer player) {
        Util.addScore(player, 100 - scorerCount);
        --scorerCount;
    }

    private static final List<Supplier<ItemStack>> TIER_ONE_FOODS = new ArrayList<>(List.of(
            () -> new ItemStack(Items.COOKED_CHICKEN), () -> new ItemStack(Items.COOKED_BEEF), () -> new ItemStack(Items.BEETROOT_SOUP),
            () -> new ItemStack(Items.BAKED_POTATO), () -> new ItemStack(Items.POISONOUS_POTATO), () -> new ItemStack(Items.COOKED_PORKCHOP),
            () -> new ItemStack(Items.GOLDEN_CARROT), () -> new ItemStack(Items.HONEY_BOTTLE), () -> new ItemStack(Items.BEETROOT)));
    private static final Map<ServerPlayerProcessor, MutableInt> playerRounds = new WeakHashMap<>();
    private static final RandomSource RANDOM = RandomSource.create();

    private static ActionSupplier<ServerPlayer> newAction(int maxRounds, Consumer<ServerPlayer> endFunc, Supplier<ActionSupplier<ServerPlayer>> recursion) {
        return ActionList.<ServerPlayer>getBuilder().action((player, handler) -> {
            Collections.shuffle(TIER_ONE_FOODS);
            for (int index = 0; index < TIER_ONE_FOODS.size(); ++index) {
                player.getInventory().setItem(index, TIER_ONE_FOODS.get(index).get());
            }

            Item goal = TIER_ONE_FOODS.get(RANDOM.nextInt(TIER_ONE_FOODS.size())).get().getItem();
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_overeating_eat").append(goal.getName())));

            ProcessorManager.get(player).emitListener(new OvereatingEventListener() {
                @Override
                public Event getEvent() {
                    return new ModEvents.EatEvent(goal);
                }

                @Override
                public Consumer<ServerPlayer> getAction() {
                    return player1 -> {
                        MutableInt score = playerRounds.get(ProcessorManager.get(player1));
                        score.add(1);
                        if (score.intValue() < maxRounds) {
                            ProcessorManager.get(player1).emitActions(recursion);
                        } else {
                            playerRounds.remove(ProcessorManager.get(player1));
                            Util.removeInventoryItemsFromPlayer(player1);
                            endFunc.accept(player1);
                        }
                    };
                }
            });
            return true;
        }).build();
    }

    public static class OvereatingEventListener implements EventListener<ServerPlayer> {
        @Override
        public Event getEvent() {
            return null;
        }

        @Override
        public Consumer<ServerPlayer> getAction() {
            return null;
        }
    }

    private static final ActionSupplier<ServerPlayer> DEFAULT_SETTINGS_ONE = newAction(5, Util::addScore, new Supplier<>() {
        @Override
        public ActionSupplier<ServerPlayer> get() {
            return DEFAULT_SETTINGS_ONE;
        }
    });

    public static final ActionSupplier<ServerActionContext> OVEREATING_PRESET_ONE = ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
        context.processors().forEach(processor -> {
            playerRounds.put(processor, new MutableInt());
            processor.emitActions(DEFAULT_SETTINGS_ONE);
        });
        return false;
    }).wait(320).action((context, handler) -> {
        context.processors().forEach(processor -> {
            Util.removeInventoryItemsFromPlayer(processor.getPlayer());
            processor.removeListener(new OvereatingEventListener());
        });
        ModServerActions.showPlayersScoreBoard(context);
        return true;
    }).build();

    private static final ActionSupplier<ServerActionContext> OVEREATING_PRESET_ONE_MAIN = ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
        ProcessorManager.getServerProcessor().emitActions(context, ModServerActions.displayCountdown(320));
        context.processors().forEach(processor -> {
            playerRounds.put(processor, new MutableInt());
            processor.emitActions(DEFAULT_SETTINGS_ONE);
        });
        return false;
    }).wait(320).action((context, handler) -> {
        context.processors().forEach(processor -> {
            Util.removeInventoryItemsFromPlayer(processor.getPlayer());
            processor.removeListener(new OvereatingEventListener());
        });
        ModServerActions.showPlayersScoreBoard(context);
        ProcessorManager.getServerProcessor().emitActions(context, ModServerActions.GAME_END);
        return true;
    }).build();

    public static final GameData DEFAULT_SETTING_ONE_GAME_DATA = new GameData(ModEvents.PLAYER_OVEREATING_READY, OVEREATING_PRESET_ONE_MAIN, new EventListener<>() {
        @Override
        public Event getEvent() {
            return ModEvents.PLAYER_OVEREATING_READY;
        }

        @Override
        public Consumer<ServerPlayer> getAction() {
            return player -> {
            };
        }
    });
}
