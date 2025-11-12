package xyz.jinenze.wuhumc.action;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.jinenze.wuhumc.util.ServerPlayerMixinGetter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProcessorManager {
    private static final Processor<ServerActionContext> serverProcessor = new Processor<>();
    private static final Map<UUID, PlayerProcessor<ServerPlayerEntity>> processors = new HashMap<>();

    public static PlayerProcessor<ServerPlayerEntity> get(ServerPlayerEntity player) {
        return ((ServerPlayerMixinGetter) player).wuhumc$getProcessor();
    }

    public static Processor<ServerActionContext> getServerProcessor() {
        return serverProcessor;
    }

    public static PlayerProcessor<ServerPlayerEntity> createOrRefresh(ServerPlayerEntity player) {
        var processor = processors.get(player.getUuid());
        if (processor != null) {
            processor.setPlayer(player);
            return processor;
        }
        processor = new PlayerProcessor<>(player);
        processors.put(player.getUuid(), processor);
        return processor;
    }

    public static void setPlayer(ServerPlayerEntity player) {
        get(player).setPlayer(player);
    }

    public static void remove(ServerPlayerEntity player) {
        processors.remove(player.getUuid());
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> {
            getServerProcessor().tick();
            for (ServerPlayerEntity player : minecraftServer.getPlayerManager().getPlayerList()) {
                get(player).tick();
            }
        });
//        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
//            remove(handler.getPlayer());
//            PlayerItemUtil.removeReadyItemFromPlayer(handler.getPlayer());
//        });
    }
}
