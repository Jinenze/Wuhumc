package xyz.jinenze.wuhumc.action;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerProcessor<T extends Player> extends Processor<T> {
    protected T player;
    private final Map<String, Object> customData = new HashMap<>();

    public boolean emitEventToFirstMatch(Event event) {
        return super.emitEventToFirstMatch(player, event);
    }

    public void emitEventToAll(Event event) {
        super.emitEventToAll(player, event);
    }

    public void emitActions(ActionProvider<T> actions) {
        super.emitActions(player, actions);
    }

    public void setPlayer(T player) {
        this.player = player;
    }

    public T getPlayer() {
        return player;
    }

    public Map<String, Object> customData() {
        return customData;
    }

    public PlayerProcessor() {
    }

    public PlayerProcessor(T player) {
        this.player = player;
    }
}
