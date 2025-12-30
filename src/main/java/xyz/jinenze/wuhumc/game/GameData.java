package xyz.jinenze.wuhumc.game;

import net.minecraft.server.level.ServerPlayer;
import xyz.jinenze.wuhumc.action.ActionSupplier;
import xyz.jinenze.wuhumc.action.Event;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.ServerActionContext;
import xyz.jinenze.wuhumc.util.Util;

public record GameData(Event onReadyEvent, ActionSupplier<ServerActionContext> gameStartAction,
                       EventListener<ServerPlayer> notReadyListener) {
    public GameData(Event onReadyEvent, ActionSupplier<ServerActionContext> gameStartAction) {
        this(onReadyEvent, gameStartAction, Util.newRecursionListener(onReadyEvent));
    }
}
