package xyz.jinenze.wuhumc.client;

import net.fabricmc.api.ClientModInitializer;
import xyz.jinenze.wuhumc.client.init.ModClientActions;
import xyz.jinenze.wuhumc.client.init.ModClientCommands;

public class WuhumcClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModClientActions.register();
        ModClientCommands.register();
    }
}
