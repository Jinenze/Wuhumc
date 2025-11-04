package xyz.jinenze.wuhumc.action;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProcessorManager {
    private static final ProcessorManager instance = new ProcessorManager();
    private final Map<UUID, ActionProcessor<ServerPlayerEntity>> processorMap = new HashMap<>();

    public ActionProcessor<ServerPlayerEntity> getProcessor(ServerPlayerEntity player) {
        var processor = processorMap.get(player.getUuid());
        if (processor != null) {
            return processor;
        }
        processor = new ActionProcessor<>(player);
        processorMap.put(player.getUuid(), processor);
        return processor;
    }

    public void setPlayer(ServerPlayerEntity player) {
        getProcessor(player).setPlayer(player);
    }

    public void remove(ServerPlayerEntity player) {
        processorMap.remove(player.getUuid());
    }

    public static ProcessorManager getInstance() {
        return instance;
    }

    private ProcessorManager() {
    }

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> getInstance().remove(handler.getPlayer()));
    }
}
