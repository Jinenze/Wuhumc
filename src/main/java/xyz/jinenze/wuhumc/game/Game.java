package xyz.jinenze.wuhumc.game;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.jinenze.wuhumc.action.Actions;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.ServerActionContext;
import xyz.jinenze.wuhumc.init.ModServerEvents;

public abstract class Game {
    private boolean running;
    private final ModServerEvents onReadyEvent;
    private final Actions<ServerActionContext> gameStartAction;
    private final EventListener<ServerPlayerEntity> notReadyListener;

    public Game(ModServerEvents onReadyEvent, Actions<ServerActionContext> gameStartAction, EventListener<ServerPlayerEntity> notReadyListener) {
        this.onReadyEvent = onReadyEvent;
        this.gameStartAction = gameStartAction;
        this.notReadyListener = notReadyListener;
    }

    public void gameStart() {
        running = true;
    }

    public void gameEnd() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public ModServerEvents getOnReadyEvent() {
        return onReadyEvent;
    }

    public Actions<ServerActionContext> getGameStartAction() {
        return gameStartAction;
    }

    public EventListener<ServerPlayerEntity> getNotReadyListener() {
        return notReadyListener;
    }
}
