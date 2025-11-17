package xyz.jinenze.wuhumc.game;

import net.minecraft.server.level.ServerPlayer;
import xyz.jinenze.wuhumc.action.*;
import xyz.jinenze.wuhumc.init.ModServerEvents;

public class Game {
    private boolean running;
    private final ModServerEvents onReadyEvent;
    private final Actions<ServerActionContext> gameStartAction;
    private final EventListener<ServerPlayer> notReadyListener;

    public boolean gameStartPlayerEjectDirection = false;

    public Game(ModServerEvents onReadyEvent, Actions<ServerActionContext> gameStartAction, EventListener<ServerPlayer> notReadyListener) {
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

    public void addScore(ServerPlayer player) {
        ProcessorManager.get(player).addScore(1);
    }

    public ModServerEvents getOnReadyEvent() {
        return onReadyEvent;
    }

    public Actions<ServerActionContext> getGameStartAction() {
        return gameStartAction;
    }

    public EventListener<ServerPlayer> getNotReadyListener() {
        return notReadyListener;
    }
}
