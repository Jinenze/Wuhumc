package xyz.jinenze.wuhumc.action;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ActionsHandler<T> {
    private final T input;
    private final ActionProvider<T> actions;
    private final Iterator<Action<T>> iterator;
    private final Map<String, Object> customData = new HashMap<>();
    private int delay;

    public ActionsHandler(T input, ActionProvider<T> actions) {
        this.input = input;
        this.actions = actions;
        this.iterator = actions.iterator();
    }

    public boolean tick() {
        if (delay > 0) {
            --delay;
            return false;
        }
        return iterator.next().run(input, this) || tick();
    }

    public ActionProvider<T> getActions() {
        return actions;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public Map<String, Object> customData() {
        return customData;
    }
}
