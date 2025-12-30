package xyz.jinenze.wuhumc.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import xyz.jinenze.wuhumc.client.WuhumcClient;
import xyz.jinenze.wuhumc.client.config.ClientConfigWrapper;
import xyz.jinenze.wuhumc.client.gui.ScoreBoard;
import xyz.jinenze.wuhumc.network.Payloads;

public class ClientNetwork {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ServerConfigS2CPayload.TYPE, (payload, context) -> {
            var config = new ClientConfigWrapper();
            config.client = WuhumcClient.config;
            config.server = payload.config();
            WuhumcClient.configHolder.setConfig(config);
        });
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ShowScoreBoardS2CPayload.TYPE, (payload, context) -> {
            ScoreBoard.processMap(payload.scores());
        });
        ClientPlayNetworking.registerGlobalReceiver(Payloads.SetRespawnMusicS2CPayload.TYPE, (payload, context) -> {
            WuhumcClient.config.respawn_music = payload.bl();
        });
    }
}
