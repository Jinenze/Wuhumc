package xyz.jinenze.wuhumc.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public record ActionList<T>(List<Action<T>> actions) implements ActionProvider<T> {

    public Iterator<Action<T>> iterator() {
        return actions.iterator();
    }

    public static <T> Builder<T> getBuilder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private final ArrayList<Action<T>> actions = new ArrayList<>();

        public Builder<T> action(Action<T> action) {
            actions.add(action);
            return this;
        }

        public Builder<T> wait(int delay) {
            return action((input, handler) -> {
                handler.setDelay(delay);
                return false;
            });
        }

        public ActionList<T> build() {
            return new ActionList<>(Collections.unmodifiableList(actions));
        }

        private Builder() {
        }
    }
}
