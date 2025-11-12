package xyz.jinenze.wuhumc.init;

import net.minecraft.server.level.ServerPlayer;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.ProcessorManager;

import java.util.function.Supplier;

public class ModEventListeners {
    public static final EventListener<ServerPlayer> NULL = new EventListener<>(ModServerEvents.NULL, player -> {
    });

    public static final EventListener<ServerPlayer> PLAYER_WSNZ_READY_PLAYER_NOT_READY = new EventListener<>(ModServerEvents.PLAYER_WSNZ_READY, player -> {
        ProcessorManager.get(player).emitListener(new Supplier<>() {
            @Override
            public EventListener<ServerPlayer> get() {
                return PLAYER_WSNZ_READY_PLAYER_NOT_READY;
            }
        });
    });

    public static final EventListener<ServerPlayer> PLAYER_FALL_VOID_WSNZ_1 = new EventListener<>(ModServerEvents.PLAYER_FALL_VOID, player -> {
       ProcessorManager.get(player).addScore(1);
    });

    public static void register() {
    }
}
