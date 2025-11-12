package xyz.jinenze.wuhumc.config;

import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import xyz.jinenze.wuhumc.Wuhumc;

@Config(name = Wuhumc.MOD_ID)
public class ServerConfigWrapper extends PartitioningSerializer.GlobalData {
    @ConfigEntry.Category("server")
    public ServerConfig server = new ServerConfig();
}
