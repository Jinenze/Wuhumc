package xyz.jinenze.wuhumc.action;

import xyz.jinenze.wuhumc.init.ModServerEvents;

import java.util.function.Consumer;

public record EventListener<T>(ModServerEvents event, Consumer<T> action) {
}
