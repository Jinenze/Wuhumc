package xyz.jinenze.wuhumc.action;

import java.util.Iterator;
import java.util.function.Supplier;

public interface ActionSupplier<T> extends Supplier<Iterator<Action<T>>> {
}
