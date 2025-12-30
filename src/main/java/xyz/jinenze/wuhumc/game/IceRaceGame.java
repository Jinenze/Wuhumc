package xyz.jinenze.wuhumc.game;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.*;
import xyz.jinenze.wuhumc.init.ModEvents;
import xyz.jinenze.wuhumc.init.ModGames;
import xyz.jinenze.wuhumc.init.ModServerActions;
import xyz.jinenze.wuhumc.util.Util;

import java.util.function.Consumer;

public class IceRaceGame extends GameSession {
    private int tickCount = 0;
    private ServerActionContext context;

    @Override
    public GameData getGameData() {
        return GAME_DATA;
    }

    @Override
    public void gameEnd() {
        super.gameEnd();
        //todo: need nuke
        ModGames.ICE_RACE = new IceRaceGame();
    }

    private static final EventListener<ServerPlayer> IN_GAME_LISTENER = Util.newRecursionListener(ModEvents.PLAYER_ICE_RACE_ARRIVE);

    private static final ActionSupplier<ServerActionContext> COUNTDOWN_ACTION = ModServerActions.newCountdownAction(2400);
    private static final ActionSupplier<ServerActionContext> TICK_COUNTER = () -> (HasNextIterator<Action<ServerActionContext>>) () -> (context, handler) -> {
        ((IceRaceGame) context.processors().getFirst().getGameSession()).tickCount += 1;
        handler.setDelay(1);
        return false;
    };

    private static final EventListener<ServerPlayer> GOAL_LISTENER = new EventListener<>() {
        @Override
        public Event getEvent() {
            return ModEvents.PLAYER_TRIGGER_OAK_PRESSURE_PLATE;
        }

        @Override
        public Consumer<ServerPlayer> getAction() {
            return PLAYER_ARRIVE_ACTION;
        }
    };

    private static final EventListener<ServerPlayer> VOID_LISTENER = new EventListener<>() {
        private final Consumer<ServerPlayer> action = player -> {
            ProcessorManager.get(player).emitListener(this);
            player.addItem(new ItemStack(Items.OAK_BOAT));
        };

        @Override
        public Event getEvent() {
            return ModEvents.PLAYER_FALL_VOID;
        }

        @Override
        public Consumer<ServerPlayer> getAction() {
            return action;
        }
    };

    private static final ActionSupplier<ServerActionContext> MAIN = ActionList.<ServerActionContext>getBuilder().action((context, handler) -> {
        ProcessorManager.getServerProcessor().emitActions(context, COUNTDOWN_ACTION);
        context.processors().forEach(processor -> {
            var player = processor.getPlayer();
            Util.setOverworldSpawnPoint(player, Wuhumc.config.game_settings_ice_race.position);
            Util.resetPlayerPosition(player);
            player.addItem(new ItemStack(Items.OAK_BOAT));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_ice_race.game_start")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            processor.emitListener(GOAL_LISTENER);
            processor.emitListener(IN_GAME_LISTENER);
            processor.emitListener(VOID_LISTENER);
        });
        ProcessorManager.getServerProcessor().emitActions(context, TICK_COUNTER);
        ((IceRaceGame) context.processors().getFirst().getGameSession()).context = context;
        return false;
    }).wait(2400).action((context, handler) -> {
        for (ServerPlayerProcessor processor : context.processors()) {
            processor.removeListener(GOAL_LISTENER);
            processor.removeListener(IN_GAME_LISTENER);
            if (processor.getCurrentScore() == 0) {
                processor.addScore(Integer.MIN_VALUE);
            }
        }
        context.processors().getFirst().getPlayer().level().getAllEntities().forEach(entity -> {
            if (entity instanceof Boat) entity.discard();
        });
        ModServerActions.showPlayersScoreBoard(context);
        ProcessorManager.getServerProcessor().emitActions(context, ModServerActions.GAME_END);
        return true;
    }).build();

    private static final Consumer<ServerPlayer> PLAYER_ARRIVE_ACTION = player -> {
        ProcessorManager.get(player).setCurrentScore(-((IceRaceGame) ProcessorManager.get(player).getGameSession()).tickCount);
        if (player.getVehicle() != null) {
            player.getVehicle().discard();
        }
        ProcessorManager.get(player).removeListener(IN_GAME_LISTENER);
        player.setGameMode(GameType.SPECTATOR);
        for (var processor : ((IceRaceGame) ProcessorManager.get(player).getGameSession()).context.processors()) {
            if (ProcessorManager.get(processor.getPlayer()).emitEventToFirstMatch(ModEvents.PLAYER_ICE_RACE_ARRIVE)) {
                return;
            }
        }
        player.level().getAllEntities().forEach(entity -> {
            if (entity instanceof Boat) entity.discard();
        });
        var context = ((IceRaceGame) ProcessorManager.get(player).getGameSession()).context;
        ProcessorManager.getServerProcessor().planToRemoveActions(Util.handlerIsAction(TICK_COUNTER));
        ProcessorManager.getServerProcessor().planToRemoveActions(Util.handlerIsAction(COUNTDOWN_ACTION));
        ProcessorManager.getServerProcessor().planToRemoveActions(handler -> handler.getActions().equals(MAIN) && handler.getInput().equals(context));
        ModServerActions.showPlayersScoreBoard(context);
        ProcessorManager.getServerProcessor().emitActions(context, ModServerActions.GAME_END);
    };

    public static final GameData GAME_DATA = new GameData(ModEvents.PLAYER_ICE_RACE_READY, MAIN);
}
