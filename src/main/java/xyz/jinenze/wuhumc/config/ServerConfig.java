package xyz.jinenze.wuhumc.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.core.BlockPos;

@Config(name = "server")
public class ServerConfig implements ConfigData {
    public boolean respawn_fly_enabled = true;

    public double heavy_weight_pressure_plate_force_factor = 10.0;

    @ConfigEntry.Gui.CollapsibleObject
    public WSNZGameSettings game_settings_wsnz = new WSNZGameSettings();

    public static class WSNZGameSettings {
        public int stage_one_max_rounds = 5;
        public int stage_two_max_rounds = 5;
        public int stage_three_max_rounds = 5;

        @ConfigEntry.Gui.Excluded
        public BlockPos position = new BlockPos(0, 0, 0);
    }

    @ConfigEntry.Gui.Excluded
    public IceRaceGameSettings game_settings_ice_race = new IceRaceGameSettings();

    public static class IceRaceGameSettings {
        @ConfigEntry.Gui.Excluded
        public BlockPos position = new BlockPos(0, 0, 0);
    }
}
