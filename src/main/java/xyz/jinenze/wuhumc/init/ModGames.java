package xyz.jinenze.wuhumc.init;

import net.minecraft.server.level.ServerPlayer;
import xyz.jinenze.wuhumc.action.Event;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.game.Game;
import xyz.jinenze.wuhumc.game.WSNZGame;

import java.util.function.Consumer;

public class ModGames {
    public static final Game NULL = new Game(ModEvents.NULL, ModServerActions.NULL_WORLD, new EventListener<>() {
        @Override
        public Event getEvent() {
            return ModEvents.NULL;
        }

        @Override
        public Consumer<ServerPlayer> getAction() {
            return (player) -> {
            };
        }
    });

    public static final WSNZGame WSNZ = new WSNZGame();

    public static void register() {
    }
}

