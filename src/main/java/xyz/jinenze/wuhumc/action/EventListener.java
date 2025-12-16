package xyz.jinenze.wuhumc.action;

import java.util.function.Consumer;

public record EventListener<T>(Event event, Consumer<T> action) {
}
