package xyz.jinenze.wuhumc.action;

import java.util.Iterator;

public interface ActionProvider<T> {
    Iterator<Action<T>> iterator();
}
