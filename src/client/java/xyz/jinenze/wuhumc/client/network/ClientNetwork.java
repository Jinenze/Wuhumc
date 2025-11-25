package xyz.jinenze.wuhumc.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import xyz.jinenze.wuhumc.client.gui.ScoreBoard;
import xyz.jinenze.wuhumc.network.Payloads;

public class ClientNetwork {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ShowScoreBoardS2CPayload.TYPE, (payload, context) -> {
            ScoreBoard.processMap(payload.scores());
        });
    }
}
