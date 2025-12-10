package xyz.jinenze.wuhumc.action;

import net.minecraft.server.level.ServerPlayer;
import xyz.jinenze.wuhumc.game.Game;
import xyz.jinenze.wuhumc.init.ModGames;
import xyz.jinenze.wuhumc.util.InventorySnapshot;

import java.util.Stack;

public class ServerPlayerProcessor extends PlayerProcessor<ServerPlayer> {
    private Game currentGame = ModGames.NULL;
    private int previousScore = 0;
    private int currentScore = 0;
    private final Stack<InventorySnapshot> inventoryCacheStack = new Stack<>();

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.previousScore = this.currentScore;
        this.currentScore = currentScore;
    }

    public void addCurrentScore(int score) {
        setCurrentScore(getCurrentScore() + score);
    }

    public int getPreviousScore() {
        return previousScore;
    }

    public void resetPreviousScore() {
        previousScore = getCurrentScore();
    }

    public void resetScore() {
        currentScore = 0;
        previousScore = 0;
    }

    public Stack<InventorySnapshot> getInventoryCacheStack() {
        return inventoryCacheStack;
    }

    public ServerPlayerProcessor(ServerPlayer player) {
        super(player);
    }
}
