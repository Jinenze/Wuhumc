package xyz.jinenze.wuhumc.init;

import net.minecraft.resources.ResourceKey;
import xyz.jinenze.wuhumc.action.Event;

public enum ModEvents implements Event {
    PLAYER_WSNZ_READY,
    PLAYER_FALL_VOID,
    PLAYER_SNEAK,
    PLAYER_ANOTHER_PLAYER_PICKUP_DIAMOND,
    PLAYER_KILLED_ANOTHER_PLAYER,
    NULL,
    ;

    public record CraftEvent(ResourceKey<?> id) implements Event {
        @Override
        public boolean equals(Object event) {
            return event instanceof CraftEvent(ResourceKey<?> id1) && id1.equals(id);
        }
    }
}
