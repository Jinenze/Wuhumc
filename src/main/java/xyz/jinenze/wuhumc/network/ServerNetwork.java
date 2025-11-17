package xyz.jinenze.wuhumc.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import xyz.jinenze.wuhumc.Wuhumc;

public class ServerNetwork {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(Payloads.ServerConfigC2SPayload.TYPE, (payload, context) -> {
            if (context.player().hasPermissions(2)) {
                Wuhumc.config = payload.config();
            }
        });
    }
}
