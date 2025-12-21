package xyz.jinenze.wuhumc.game;

import net.minecraft.server.level.ServerPlayer;
import xyz.jinenze.wuhumc.action.ActionList;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.action.ServerActionContext;
import xyz.jinenze.wuhumc.init.ModEvents;

public class Game {
    private boolean running;
    private final ModEvents onReadyEvent;
    private final ActionList<ServerActionContext> gameStartAction;
    private final EventListener<ServerPlayer> notReadyListener;

//    public boolean gameStartPlayerEjectDirection = false;

    public Game(ModEvents onReadyEvent, ActionList<ServerActionContext> gameStartAction, EventListener<ServerPlayer> notReadyListener) {
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
        ProcessorManager.get(player).addCurrentScore(1);
    }

    public ModEvents getOnReadyEvent() {
        return onReadyEvent;
    }

    public ActionList<ServerActionContext> getGameStartAction() {
        return gameStartAction;
    }

    public EventListener<ServerPlayer> getNotReadyListener() {
        return notReadyListener;
    }
}
