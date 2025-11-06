package xyz.jinenze.wuhumc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.config.ServerConfig;
import xyz.jinenze.wuhumc.init.ModCommands;
import xyz.jinenze.wuhumc.init.ModItems;
import xyz.jinenze.wuhumc.init.ModServerActions;

public class Wuhumc implements ModInitializer {
    public static final String MOD_ID = "wuhumc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ServerConfig config = new ServerConfig();

    @Override
    public void onInitialize() {
        ProcessorManager.register();
        ModCommands.register();
        ModItems.register();
        ModServerActions.register();
    }
}
