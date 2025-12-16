package xyz.jinenze.wuhumc.action;

import xyz.jinenze.wuhumc.init.ModServerEvents;

import java.util.function.Consumer;

public record EventListener<T>(Event event, Consumer<T> action) {
}
