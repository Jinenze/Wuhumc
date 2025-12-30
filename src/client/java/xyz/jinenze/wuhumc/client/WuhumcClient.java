package xyz.jinenze.wuhumc.client;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.fabricmc.api.ClientModInitializer;
import xyz.jinenze.wuhumc.client.config.ClientConfig;
import xyz.jinenze.wuhumc.client.config.ClientConfigWrapper;
import xyz.jinenze.wuhumc.client.init.ModClientActions;
import xyz.jinenze.wuhumc.client.init.ModClientCommands;
import xyz.jinenze.wuhumc.client.network.ClientNetwork;

public class WuhumcClient implements ClientModInitializer {
    public static ConfigHolder<ClientConfigWrapper> configHolder;
    public static ClientConfig config;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ClientConfigWrapper.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));
        configHolder = AutoConfig.getConfigHolder(ClientConfigWrapper.class);
        config = configHolder.getConfig().client;
        ModClientActions.register();
        ModClientCommands.register();
        ClientNetwork.register();
    }
}
