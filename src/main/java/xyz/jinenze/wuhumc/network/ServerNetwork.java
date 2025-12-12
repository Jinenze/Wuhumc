package xyz.jinenze.wuhumc.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import xyz.jinenze.wuhumc.Wuhumc;

import java.security.Permissions;

public class ServerNetwork {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(Payloads.ServerConfigC2SPayload.TYPE, (payload, context) -> {
            if (context.player().permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS))) {
                Wuhumc.config = payload.config();
            }
        });
    }
}
