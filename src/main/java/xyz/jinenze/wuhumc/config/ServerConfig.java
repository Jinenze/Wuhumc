package xyz.jinenze.wuhumc.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "server")
public class ServerConfig implements ConfigData {
    public boolean game_start_player_eject_direction = false;
    public boolean respawnFlyEnabled = false;
    public GamePosition GAME_POSITION_WSNZ = new GamePosition();

    public static class GamePosition {
        public double x = 0;
        public double y = 0;
        public double z = 0;
    }
}
