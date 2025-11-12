package xyz.jinenze.wuhumc.game;

import net.minecraft.server.level.ServerPlayer;
import xyz.jinenze.wuhumc.action.ActionProvider;
import xyz.jinenze.wuhumc.action.Actions;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.ServerActionContext;
import xyz.jinenze.wuhumc.init.ModServerActions;
import xyz.jinenze.wuhumc.init.ModServerEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class WSNZGame extends Game {
    private final List<ActionProvider<ServerActionContext>> subActionList = new ArrayList<>(List.of(
            ModServerActions.WSNZ_1
    ));
    private Iterator<ActionProvider<ServerActionContext>> iterator;

    public WSNZGame(ModServerEvents onReadyEvent, Actions<ServerActionContext> gameStartAction, EventListener<ServerPlayer> notReadyListener) {
        super(onReadyEvent, gameStartAction, notReadyListener);
        reroll();
    }

    public Iterator<ActionProvider<ServerActionContext>> getIterator() {
        return iterator;
    }

    public void reroll() {
        Collections.shuffle(subActionList);
        iterator = subActionList.iterator();
    }
}
