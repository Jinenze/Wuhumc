package xyz.jinenze.wuhumc.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.config.ServerConfigWrapper;

public class ServerNetwork {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(Payloads.ServerConfigC2SPayload.TYPE, (payload, context) -> {
            if (context.player().permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS))) {
                Wuhumc.config = payload.config();
                var config = new ServerConfigWrapper();
                config.server = payload.config();
                Wuhumc.configHolder.setConfig(config);
                Wuhumc.configHolder.save();
            }
        });
    }
}
