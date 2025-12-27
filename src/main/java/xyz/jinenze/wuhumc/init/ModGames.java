package xyz.jinenze.wuhumc.init;

import net.minecraft.server.level.ServerPlayer;
import xyz.jinenze.wuhumc.action.Event;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.game.GameData;
import xyz.jinenze.wuhumc.game.GameSession;
import xyz.jinenze.wuhumc.game.WSNZGameSession;

import java.util.function.Consumer;

public class ModGames {
    public static final GameSession NULL = new GameSession() {
        private static final GameData GAME_DATA = new GameData(ModEvents.NULL, ModServerActions.NULL_WORLD, new EventListener<>() {
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

        @Override
        public GameData getGameData() {
            return GAME_DATA;
        }
    };

    public static WSNZGameSession WSNZ = new WSNZGameSession();

    public static void register() {
    }
}

