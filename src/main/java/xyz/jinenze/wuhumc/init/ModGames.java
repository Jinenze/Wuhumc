package xyz.jinenze.wuhumc.init;

import xyz.jinenze.wuhumc.action.Game;

public class ModGames {
    public static final Game WSNZ = new Game(ModServerEvents.WSNZ_PLAYER_READY, ModServerEvents.WSNZ_GAME_START, ModEventListeners.WSNZ_NOT_READY, ModEventListeners.WSNZ_READY);

    public void register() {
    }
}

