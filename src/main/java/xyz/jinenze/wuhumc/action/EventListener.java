package xyz.jinenze.wuhumc.action;

import java.util.function.Consumer;

public interface EventListener<T> {
    Event getEvent();

    Consumer<T> getAction();
}
