package xyz.jinenze.wuhumc.init;

import xyz.jinenze.wuhumc.action.Game;

public class ModGames {
    public static final Game NULL = new Game(ModServerEvents.NULL, ModServerEvents.NULL, ModEventListeners.NULL, ModEventListeners.NULL);

    public static final Game WSNZ = new Game(ModServerEvents.PLAYER_WSNZ_READY, ModServerEvents.GAME_WSNZ_START, ModEventListeners.PLAYER_WSNZ_READY_PLAYER_NOT_READY, ModEventListeners.GAME_WSNZ_START_COUNTDOWN);

    public void register() {
    }
}

