package xyz.jinenze.wuhumc.action;

import net.minecraft.world.entity.player.Player;

public class PlayerProcessor<T extends Player> extends Processor<T> {
    protected T player;

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

    public PlayerProcessor() {
    }

    public PlayerProcessor(T player) {
        this.player = player;
    }
}
