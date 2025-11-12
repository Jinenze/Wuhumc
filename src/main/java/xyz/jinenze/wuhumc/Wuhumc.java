package xyz.jinenze.wuhumc;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.config.ServerConfig;
import xyz.jinenze.wuhumc.config.ServerConfigWrapper;
import xyz.jinenze.wuhumc.init.ModCommands;
import xyz.jinenze.wuhumc.init.ModEventListeners;
import xyz.jinenze.wuhumc.init.ModItems;
import xyz.jinenze.wuhumc.init.ModServerActions;

public class Wuhumc implements ModInitializer {
    public static final String MOD_ID = "wuhumc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ServerConfig config;

    @Override
    public void onInitialize() {
        AutoConfig.register(ServerConfigWrapper.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));
        config = AutoConfig.getConfigHolder(ServerConfigWrapper.class).getConfig().server;
        ProcessorManager.register();
        ModCommands.register();
        ModEventListeners.register();
        ModItems.register();
        ModServerActions.register();
    }
}
