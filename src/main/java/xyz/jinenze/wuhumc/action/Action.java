package xyz.jinenze.wuhumc.action;

public interface Action<T> {
    boolean run(T input, ActionsHandler<T> handler);
}
