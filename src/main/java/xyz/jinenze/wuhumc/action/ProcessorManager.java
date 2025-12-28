package xyz.jinenze.wuhumc.action;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import xyz.jinenze.wuhumc.util.ServerPlayerMixinGetter;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class ProcessorManager {
    private static final Processor<ServerActionContext> serverProcessor = new Processor<>();
    private static final Map<UUID, ServerPlayerProcessor> processors = new WeakHashMap<>();

    public static ServerPlayerProcessor get(ServerPlayer player) {
        return ((ServerPlayerMixinGetter) player).wuhumc$getProcessor();
    }

    public static Processor<ServerActionContext> getServerProcessor() {
        return serverProcessor;
    }

    public static ServerPlayerProcessor createOrRefresh(ServerPlayer player) {
        var processor = processors.get(player.getUUID());
        if (processor != null) {
            processor.setPlayer(player);
            return processor;
        }
        processor = new ServerPlayerProcessor(player);
        processors.put(player.getUUID(), processor);
        return processor;
    }

    public static void setPlayer(ServerPlayer player) {
        get(player).setPlayer(player);
    }

//    public static void remove(ServerPlayer player) {
//        processors.remove(player.getUUID());
//    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> {
            getServerProcessor().tick();
            for (ServerPlayer player : minecraftServer.getPlayerList().getPlayers()) {
                get(player).tick();
            }
        });
//        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
//            remove(handler.getPlayer());
//            PlayerItemUtil.removeReadyItemFromPlayer(handler.getPlayer());
//        });
    }
}
