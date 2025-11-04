package xyz.jinenze.wuhumc.init;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.jinenze.wuhumc.action.Actions;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.ProcessorManager;

import java.util.function.Supplier;

public class ModEventListeners {
    public static final EventListener<ServerPlayerEntity> fallVoid = new EventListener<>(ModServerEvents.FALL_VOID, ModServerActions.fallVoid);

    public static final EventListener<ServerPlayerEntity> WSNZ_NOT_READY = new EventListener<>(ModServerEvents.WSNZ_PLAYER_READY, Actions.<ServerPlayerEntity>getBuilder().action((player, handler) -> {
                ProcessorManager.getInstance().getProcessor(player).listen(new Supplier<>() {
                    @Override
                    public EventListener<ServerPlayerEntity> get() {
                        return WSNZ_NOT_READY;
                    }
                });
                return true;
            }
    ).build());

    public static final EventListener<ServerPlayerEntity> WSNZ_READY = new EventListener<>(ModServerEvents.WSNZ_GAME_START, ModServerActions.dumbActions);

    public static void register() {
    }
}
