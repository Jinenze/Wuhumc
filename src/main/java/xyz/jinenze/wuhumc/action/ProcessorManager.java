package xyz.jinenze.wuhumc.action;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProcessorManager {
    private static final ProcessorManager instance = new ProcessorManager();
    private final Map<UUID, ActionProcessor<ServerPlayerEntity>> processors = new HashMap<>();

    public ActionProcessor<ServerPlayerEntity> get(ServerPlayerEntity player) {
        return ((ServerMixinGetter) player).wuhumc$getProcessor();
    }

    public ActionProcessor<ServerPlayerEntity> createOrRefresh(ServerPlayerEntity player) {
        var processor = processors.get(player.getUuid());
        if (processor != null) {
            processor.setPlayer(player);
            return processor;
        }
        processor = new ActionProcessor<>(player);
        processors.put(player.getUuid(), processor);
        return processor;
    }

    public void setPlayer(ServerPlayerEntity player) {
        get(player).setPlayer(player);
    }

    public void remove(ServerPlayerEntity player) {
        processors.remove(player.getUuid());
    }

    public static ProcessorManager getInstance() {
        return instance;
    }

    private ProcessorManager() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> {
            for (ServerPlayerEntity player : minecraftServer.getPlayerManager().getPlayerList()) {
                ProcessorManager.getInstance().get(player).tick();
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> getInstance().remove(handler.getPlayer()));
    }
}
