package xyz.jinenze.wuhumc.action;


import java.util.Iterator;

public class ActionsHandler<T> {
    private final T input;
    private final ActionSupplier<T> actions;
    private final Iterator<Action<T>> iterator;
    private int delay;

    public ActionsHandler(T input, ActionSupplier<T> actions) {
        this.input = input;
        this.actions = actions;
        this.iterator = actions.get();
    }

    public boolean tick() {
        if (delay > 0) {
            --delay;
            return false;
        }
        return iterator.next().run(input, this) || tick();
    }

    public ActionSupplier<T> getActions() {
        return actions;
    }

    public T getInput() {
        return input;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
