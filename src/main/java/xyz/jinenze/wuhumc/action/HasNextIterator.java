package xyz.jinenze.wuhumc.action;

import java.util.Iterator;

public interface HasNextIterator<T> extends Iterator<T> {
    @Override
    default boolean hasNext() {
        return true;
    }
}
