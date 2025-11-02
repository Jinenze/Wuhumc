package xyz.jinenze.wuhumc.client;

import net.fabricmc.api.ClientModInitializer;
import xyz.jinenze.wuhumc.client.init.ModKeyBinds;

public class WuhumcClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModKeyBinds.register();
    }
}
