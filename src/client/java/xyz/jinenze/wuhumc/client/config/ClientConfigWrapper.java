package xyz.jinenze.wuhumc.client.config;

import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.config.ServerConfig;

@Config(name = Wuhumc.MOD_ID)
public class ClientConfigWrapper extends PartitioningSerializer.GlobalData {
    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.TransitiveObject
    public ClientConfig client = new ClientConfig();

    @ConfigEntry.Category("server")
    @ConfigEntry.Gui.TransitiveObject
    public ServerConfig server = new ServerConfig();
}
