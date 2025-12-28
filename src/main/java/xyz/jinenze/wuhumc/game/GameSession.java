package xyz.jinenze.wuhumc.game;

import net.minecraft.server.level.ServerPlayer;

public abstract class GameSession {
    private boolean running;

    public boolean isRunning() {
        return running;
    }

    public void gameStart() {
        running = true;
    }

    public void gameEnd() {
        running = false;
    }

    public abstract GameData getGameData();

    public void addScore(ServerPlayer player) {
    }
}
