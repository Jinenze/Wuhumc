package xyz.jinenze.wuhumc.action;

import net.minecraft.world.entity.player.Player;
import xyz.jinenze.wuhumc.init.ModServerEvents;

public class PlayerProcessor<T extends Player> extends Processor<T> {
    protected T player;

    public boolean event(ModServerEvents event) {
        return super.event(player, event);
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
