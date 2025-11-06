package xyz.jinenze.wuhumc.init;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.ProcessorManager;

import java.util.function.Supplier;

public class ModEventListeners {
    public static final EventListener<ServerPlayerEntity> NULL = new EventListener<>(ModServerEvents.NULL, player -> {
    });

    public static final EventListener<ServerPlayerEntity> PLAYER_WSNZ_READY_PLAYER_NOT_READY = new EventListener<>(ModServerEvents.PLAYER_WSNZ_READY, player -> {
        ProcessorManager.getInstance().get(player).emitListener(new Supplier<>() {
            @Override
            public EventListener<ServerPlayerEntity> get() {
                return PLAYER_WSNZ_READY_PLAYER_NOT_READY;
            }
        });
    });

    public static final EventListener<ServerPlayerEntity> GAME_WSNZ_START_COUNTDOWN = new EventListener<>(ModServerEvents.GAME_WSNZ_START, player -> {
        ProcessorManager.getInstance().get(player).emitActions(ModServerActions.GAME_COUNTDOWN);
    });

    public static void register() {
    }
}
