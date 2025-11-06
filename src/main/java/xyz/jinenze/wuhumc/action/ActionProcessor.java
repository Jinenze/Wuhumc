package xyz.jinenze.wuhumc.action;

import net.minecraft.entity.player.PlayerEntity;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.init.ModGames;
import xyz.jinenze.wuhumc.init.ModServerEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class ActionProcessor<T extends PlayerEntity> {
    private T player;
    private Game currentGame = ModGames.NULL;
    private boolean actionProcessing;
    private boolean listenerProcessing;
    private final ArrayList<ActionsHandler> handlers = new ArrayList<>();
    private final ArrayList<ActionsHandler> handlersCache = new ArrayList<>();
    private final ArrayList<EventListener<T>> listeners = new ArrayList<>();
    private final ArrayList<EventListener<T>> listenersCache = new ArrayList<>();

    public void tick() {
        actionProcessing = true;
        handlers.removeIf(ActionsHandler::tick);
        while (!handlersCache.isEmpty()) {
            List<ActionsHandler> cache = new ArrayList<>(handlersCache);
            handlersCache.clear();
            cache.removeIf(ActionsHandler::tick);
            handlers.addAll(cache);
        }
        actionProcessing = false;
    }

    public boolean event(ModServerEvents event) {
        Wuhumc.LOGGER.info("Event dispatch: {}", event.toString());
        listenerProcessing = true;
        boolean result = listeners.removeIf((listener) -> {
            if (listener.event().equals(event)) {
                listener.action().accept(player);
                return true;
            }
            return false;
        });
        listeners.addAll(listenersCache);
        listenersCache.clear();
        listenerProcessing = false;
        return result;
    }

    public void emitActions(ActionProvider<T> actions) {
        Wuhumc.LOGGER.info("Action emit: {}", actions.toString());
        Wuhumc.LOGGER.info(actionProcessing ? "handlersCache" : "handlers");
        if (actionProcessing) {
           handlersCache.add(new ActionsHandler(actions));
           return;
        }
        handlers.add(new ActionsHandler(actions));
    }

    public void emitListener(EventListener<T> listener) {
        Wuhumc.LOGGER.info("Listener emit{}", listener.toString());
        Wuhumc.LOGGER.info(actionProcessing ? "listenersCache" : "listeners");
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

    public void removeListener(EventListener<T> listener) {
        Wuhumc.LOGGER.info("Listener removement: {}", listener.toString());
        listeners.removeIf(listener1 -> {
            Wuhumc.LOGGER.info(listener1.equals(listener) ? "true" : "false");
            return listener1.equals(listener);
        });
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }

    public void setPlayer(T player) {
        this.player = player;
    }

    public ActionProcessor() {
    }

    public ActionProcessor(T player) {
        this.player = player;
    }

    public class ActionsHandler {
        private final Iterator<Action<T>> iterator;
        private int delay;

        private ActionsHandler(ActionProvider<T> actions) {
            this.iterator = actions.iterator();
        }

        private boolean tick() {
            if (delay > 0) {
                --delay;
                return false;
            }
            return iterator.next().run(player, this) || tick();
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }
    }
}
