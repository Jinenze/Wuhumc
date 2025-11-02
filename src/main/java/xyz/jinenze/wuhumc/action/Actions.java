package xyz.jinenze.wuhumc.action;

import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public record Actions<T extends PlayerEntity>(List<Action<T>> actions) implements ActionProvider<T> {

    public Iterator<Action<T>> iterator() {
        return actions.iterator();
    }

    public static <T extends PlayerEntity> Builder<T> getBuilder() {
        return new Builder<>();
    }

    public static class Builder<T extends PlayerEntity> {
        private final ArrayList<Action<T>> actions = new ArrayList<>();

        public Builder<T> action(Action<T> action) {
            actions.add(action);
            return this;
        }

        public Builder<T> wait(int delay) {
            return action((player, handler) -> {
                handler.setDelay(delay);
                return false;
            });
        }

        public Actions<T> build() {
            return new Actions<T>(Collections.unmodifiableList(actions));
        }

        private Builder() {
        }
    }
}
