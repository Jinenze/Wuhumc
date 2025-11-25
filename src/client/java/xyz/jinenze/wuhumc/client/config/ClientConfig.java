package xyz.jinenze.wuhumc.client.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import xyz.jinenze.wuhumc.client.gui.EasingMethod;

@Config(name = "client")
public class ClientConfig implements ConfigData {
    public boolean respawn_music = true;
    @ConfigEntry.Gui.CollapsibleObject
    public scoreBoard score_board = new scoreBoard();

    public static class scoreBoard {
        public int width = 150;
        public int cap_1 = 60;
        public int cap_2 = 80;
        public float element_cap = 10;
        public float element_time_offset = 100;
        public EasingMethod.EasingMethodImpl in_easing_method = EasingMethod.EasingMethodImpl.CIRC;
        public EasingMethod.EasingMethodImpl out_easing_method = EasingMethod.EasingMethodImpl.CIRC;
        public float half_visible_time = 1000;
        public float total_visible_time = 2000;
    }
}
