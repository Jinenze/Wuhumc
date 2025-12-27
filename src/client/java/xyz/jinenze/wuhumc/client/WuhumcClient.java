package xyz.jinenze.wuhumc.client;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.fabricmc.api.ClientModInitializer;
import xyz.jinenze.wuhumc.client.config.ClientConfig;
import xyz.jinenze.wuhumc.client.config.ClientConfigWrapper;
import xyz.jinenze.wuhumc.client.init.ModClientActions;
import xyz.jinenze.wuhumc.client.init.ModClientCommands;
import xyz.jinenze.wuhumc.client.network.ClientNetwork;
import xyz.jinenze.wuhumc.config.ServerConfig;

public class WuhumcClient implements ClientModInitializer {
    public static ClientConfig config;
    public static ServerConfig serverConfig;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ClientConfigWrapper.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));
        config = AutoConfig.getConfigHolder(ClientConfigWrapper.class).getConfig().client;
        serverConfig = AutoConfig.getConfigHolder(ClientConfigWrapper.class).getConfig().server;
        ModClientActions.register();
        ModClientCommands.register();
        ClientNetwork.register();
    }
}
