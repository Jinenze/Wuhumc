package xyz.jinenze.wuhumc.client.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "client")
public class ClientConfig implements ConfigData {
    public double xz = 35d;
    public double y = 0.45d;
}
