package xyz.jinenze.wuhumc.action;


import java.util.Iterator;

public class ActionsHandler<T> {
    private final T input;
    private final ActionProvider<T> actions;
    private final Iterator<Action<T>> iterator;
    private int delay;

    ActionsHandler(T input, ActionProvider<T> actions) {
        this.input = input;
        this.actions = actions;
        this.iterator = actions.iterator();
    }

    boolean tick() {
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
}
