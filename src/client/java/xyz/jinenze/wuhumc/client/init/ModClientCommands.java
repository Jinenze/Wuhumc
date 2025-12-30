package xyz.jinenze.wuhumc.client.init;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import xyz.jinenze.wuhumc.client.WuhumcClient;
import xyz.jinenze.wuhumc.client.action.ClientMixinGetter;
import xyz.jinenze.wuhumc.network.Payloads;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ModClientCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                literal("wuhumcclient").executes(context -> {
                            ((ClientMixinGetter) context.getSource().getPlayer()).wuhumc$getProcessor().emitActions(ModClientActions.respawnMusic);
                            return 1;
                        })
                        .then(literal("config")
                                .then(literal("send"
                                ).executes(context -> {
                                    ClientPlayNetworking.send(new Payloads.ServerConfigC2SPayload(WuhumcClient.configHolder.getConfig().server));
                                    return 1;
                                })))
                        .then(literal("off").executes(context -> {
                            WuhumcClient.config.respawn_music = false;
                            return 1;
                        }))
                        .then(literal("on").executes(context -> {
                            WuhumcClient.config.respawn_music = true;
                            return 1;
                        }))
        ));
    }
}
