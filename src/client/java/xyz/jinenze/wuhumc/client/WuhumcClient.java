package xyz.jinenze.wuhumc.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.jinenze.wuhumc.client.init.ModClientActions;
import xyz.jinenze.wuhumc.client.init.ModClientCommands;
import xyz.jinenze.wuhumc.client.init.ModKeyBinds;

public class WuhumcClient implements ClientModInitializer {
    public static final String MOD_ID = "wuhumc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ModClientActions.register();
        ModClientCommands.register();
        ModKeyBinds.register();
    }
}
