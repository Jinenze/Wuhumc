package xyz.jinenze.wuhumc.init;

import xyz.jinenze.wuhumc.game.Game;
import xyz.jinenze.wuhumc.game.WSNZGame;

public class ModGames {
    public static final Game NULL = new Game(ModServerEvents.NULL, ModServerActions.NULL_WORLD, ModEventListeners.NULL);

    public static final WSNZGame WSNZ = new WSNZGame(ModServerEvents.PLAYER_WSNZ_READY, ModServerActions.WSNZ_MAIN, ModEventListeners.PLAYER_WSNZ_READY_PLAYER_NOT_READY);

    public void register() {
    }
}

