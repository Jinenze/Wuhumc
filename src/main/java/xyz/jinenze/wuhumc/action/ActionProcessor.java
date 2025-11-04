package xyz.jinenze.wuhumc.action;

import net.minecraft.entity.player.PlayerEntity;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.init.ModServerEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Supplier;

public class ActionProcessor<T extends PlayerEntity> {
    private T player;
    private Game currentGame;
    private final ArrayList<ActionsHandler> handlers = new ArrayList<>();
    private final ArrayList<EventListener<T>> listeners = new ArrayList<>();

    public void tick() {
        handlers.removeIf(ActionsHandler::tick);
    }

    public boolean emitEvent(ModServerEvents event) {
        return listeners.removeIf((listener) -> {
            if (listener.event().equals(event)) {
                emitActions(listener.action());
                return true;
            }
            return false;
        });
    }

    public void emitActions(ActionProvider<T> actions) {
        handlers.add(new ActionsHandler(actions));
        Wuhumc.LOGGER.info("123123123");
    }

    public void listen(EventListener<T> listener) {
        listeners.add(listener);
    }

    public void listen(Supplier<EventListener<T>> listener) {
        listen(listener.get());
    }

    public void clearActions() {
        handlers.clear();
    }

    public void clearListeners() {
        listeners.clear();
    }

    public void removeListener(EventListener<T> listener) {
        var iterator = listeners.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(listener)) {
                iterator.remove();
                return;
            }
        }
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
            return iterator.next().run(player, this) || !iterator.hasNext() || tick();
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }
    }
}
