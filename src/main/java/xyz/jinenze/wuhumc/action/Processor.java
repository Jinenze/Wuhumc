package xyz.jinenze.wuhumc.action;

import xyz.jinenze.wuhumc.init.ModServerEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Processor<T> {
    private boolean actionProcessing;
    private boolean listenerProcessing;
    private final ArrayList<ActionsHandler<T>> handlers = new ArrayList<>();
    private final ArrayList<ActionsHandler<T>> handlersCache = new ArrayList<>();
    private final ArrayList<ActionProvider<T>> needRemoveActions = new ArrayList<>();
    private final ArrayList<EventListener<T>> listeners = new ArrayList<>();
    private final ArrayList<EventListener<T>> listenersCache = new ArrayList<>();

    public void tick() {
        actionProcessing = true;
        needRemoveActions.removeIf(actions -> {
            handlers.removeIf(handler -> handler.getActions().equals(actions));
            return true;
        });
        handlers.removeIf(ActionsHandler::tick);
        while (!handlersCache.isEmpty()) {
            List<ActionsHandler<T>> cache = new ArrayList<>(handlersCache);
            handlersCache.clear();
            cache.removeIf(ActionsHandler::tick);
            handlers.addAll(cache);
        }
        actionProcessing = false;
    }

    public boolean event(T input, ModServerEvents event) {
        // Wuhumc.LOGGER.info("Event dispatch: {}", event.toString());
        listenerProcessing = true;
        boolean result = listeners.removeIf((listener) -> {
            if (listener.event().equals(event)) {
                listener.action().accept(input);
                return true;
            }
            return false;
        });
        listeners.addAll(listenersCache);
        listenersCache.clear();
        listenerProcessing = false;
        return result;
    }

    public void emitActions(T input, ActionProvider<T> actions) {
        // Wuhumc.LOGGER.info("Action emit: {}", actions.toString());
        // Wuhumc.LOGGER.info(actionProcessing ? "handlersCache" : "handlers");
        if (actionProcessing) {
            handlersCache.add(new ActionsHandler<>(input, actions));
            return;
        }
        handlers.add(new ActionsHandler<>(input, actions));
    }

    public void emitListener(EventListener<T> listener) {
        // Wuhumc.LOGGER.info("Listener emit: {}", listener.toString());
        // Wuhumc.LOGGER.info(actionProcessing ? "listenersCache" : "listeners");
        if (listenerProcessing) {
            listenersCache.add(listener);
            return;
        }
        listeners.add(listener);
    }

    public void emitListener(Supplier<EventListener<T>> listener) {
        emitListener(listener.get());
    }

    public void clearActions() {
        handlers.clear();
    }

    public void clearListeners() {
        listeners.clear();
    }

    public void planToRemoveRunningActions(ActionProvider<T> actions) {
        needRemoveActions.add(actions);
    }

    public void planToRemoveRunningActions(Supplier<ActionProvider<T>> actions) {
        planToRemoveRunningActions(actions.get());
    }

    public void removeListener(EventListener<T> listener) {
        // Wuhumc.LOGGER.info("Listener removement: {}", listener.toString());
        listeners.removeIf(listener1 -> {
            // Wuhumc.LOGGER.info(listener1.equals(listener) ? "true" : "false");
            return listener1.equals(listener);
        });
    }

    public boolean listenersContain(List<EventListener<T>> listeners) {

        return this.listeners.containsAll(listeners);
    }

    public Processor() {
    }
}
