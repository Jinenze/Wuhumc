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

    public static void addScoreAndShowMessage(ServerPlayer player) {
        ProcessorManager.get(player).getCurrentGame().addScore(player);
    }

    public static final EventListener<ServerPlayer> PLAYER_FALL_VOID_WSNZ_1 = new EventListener<>(ModServerEvents.PLAYER_FALL_VOID, ModEventListeners::addScoreAndShowMessage);

    public static final EventListener<ServerPlayer> PLAYER_SHIFT_DOWN_WSNZ_2 = new EventListener<>(ModServerEvents.PLAYER_SHIFT_DOWN, ModEventListeners::addScoreAndShowMessage);

    public static final EventListener<ServerPlayer> PLAYER_ANOTHER_PLAYER_PICKUP_DIAMOND_WSNZ_3 = new EventListener<>(ModServerEvents.PLAYER_ANOTHER_PLAYER_PICKUP_DIAMOND, ModEventListeners::addScoreAndShowMessage);

    public static final EventListener<ServerPlayer> PLAYER_CRAFTED_DIAMOND_AXE_WSNZ_4 = new EventListener<>(ModServerEvents.PLAYER_CRAFTED_DIAMOND_AXE, ModEventListeners::addScoreAndShowMessage);

    public static void register() {
    }
}
