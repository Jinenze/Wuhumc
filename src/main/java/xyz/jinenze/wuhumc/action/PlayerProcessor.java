package xyz.jinenze.wuhumc.action;

import net.minecraft.world.entity.player.Player;
import xyz.jinenze.wuhumc.game.Game;
import xyz.jinenze.wuhumc.init.ModGames;
import xyz.jinenze.wuhumc.init.ModServerEvents;

public class PlayerProcessor<T extends Player> extends Processor<T> {
    private T player;
    private Game currentGame = ModGames.NULL;
    private int previousScore = 0;
    private int currentScore = 0;

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }

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

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.previousScore = this.currentScore;
        this.currentScore = currentScore;
    }

    public void addScore(int score) {
        setCurrentScore(getCurrentScore() + score);
    }

    public void minusScore(int score) {
        setCurrentScore(getCurrentScore() - score);
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

    public PlayerProcessor() {
    }

    public PlayerProcessor(T player) {
        this.player = player;
    }
}
