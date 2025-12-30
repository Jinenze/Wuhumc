package xyz.jinenze.wuhumc.action;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Processor<T> {
    private boolean actionProcessing;
    private final List<ActionsHandler<T>> handlers = new ArrayList<>();
    private final List<ActionsHandler<T>> handlersCache = new ArrayList<>();
    private final List<Function<ActionsHandler<T>, Boolean>> needRemoveHandlerPredicates = new ArrayList<>();
    private final List<EventListener<T>> listeners = new ArrayList<>();
    private final Deque<Consumer<T>> listenerActionQueue = new ArrayDeque<>();

    public void tick() {
        actionProcessing = true;
        needRemoveHandlerPredicates.removeIf(predicate -> {
            handlers.removeIf(predicate::apply);
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

    public boolean emitEventToFirstMatch(T input, Event event) {
//        Wuhumc.LOGGER.info("processor: {}, event: {}", getClass(), event.toString());
        var iterator = listeners.iterator();
        boolean result = false;
        while (iterator.hasNext()) {
            var listener = iterator.next();
            if (listener.getEvent().equals(event)) {
                listenerActionQueue.add(listener.getAction());
                iterator.remove();
                result = true;
                break;
            }
        }
        while (!listenerActionQueue.isEmpty()) {
            listenerActionQueue.pop().accept(input);
        }
        return result;
    }

    public void emitEventToAll(T input, Event event) {
//        Wuhumc.LOGGER.info("processor: {}, event: {}", getClass(), event.toString());
        var iterator = listeners.iterator();
        while (iterator.hasNext()) {
            var listener = iterator.next();
            if (listener.getEvent().equals(event)) {
                listenerActionQueue.add(listener.getAction());
                iterator.remove();
            }
        }
        while (!listenerActionQueue.isEmpty()) {
            listenerActionQueue.pop().accept(input);
        }
    }

    public void emitActions(T input, ActionSupplier<T> actions) {
//        Wuhumc.LOGGER.info("processor: {}, action: {}", getClass(), actions.toString());
        if (actionProcessing) {
            handlersCache.add(new ActionsHandler<>(input, actions));
            return;
        }
        handlers.add(new ActionsHandler<>(input, actions));
    }

    public void emitListener(EventListener<T> listener) {
//        Wuhumc.LOGGER.info("processor: {}, listener: {}", getClass(), listener.toString());
        listeners.add(listener);
    }

    public void clearActions() {
        handlers.clear();
    }

    public void clearListeners() {
        listeners.clear();
    }

    public void planToRemoveActions(Function<ActionsHandler<T>, Boolean> predicate) {
        needRemoveHandlerPredicates.add(predicate);
    }

    public void removeListener(EventListener<T> listener) {
        listeners.removeIf(listener::equals);
    }

    public Processor() {
    }
}
