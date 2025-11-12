package xyz.jinenze.wuhumc.client.init;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import xyz.jinenze.wuhumc.client.WuhumcClient;
import xyz.jinenze.wuhumc.client.action.ClientMixinGetter;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ModClientCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("wuhumcclient").executes(context -> {
                        ((ClientMixinGetter) context.getSource().getPlayer()).wuhumc$getProcessor().emitActions(ModClientActions.respawnMusic);
                        return 1;
                    }).then(literal("off").executes(context -> {
                        WuhumcClient.config.respawnMusic = false;
                        return 1;
                    })).then(literal("on").executes(context -> {
                        WuhumcClient.config.respawnMusic = true;
                        return 1;
                    }))
            );
        });
    }
}
