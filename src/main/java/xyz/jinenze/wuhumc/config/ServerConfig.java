package xyz.jinenze.wuhumc.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "server")
public class ServerConfig implements ConfigData {
    public boolean respawn_fly_enabled = true;

    @ConfigEntry.Gui.CollapsibleObject
    public WSNZGameSettings game_settings_wsnz = new WSNZGameSettings();

    public static class GamePosition {
        public int x = 0;
        public int y = 0;
        public int z = 0;
    }

    public static class WSNZGameSettings {
        public int max_games = 10;
        @ConfigEntry.Gui.CollapsibleObject
        public GamePosition game_position_wsnz = new GamePosition();
    }
}
