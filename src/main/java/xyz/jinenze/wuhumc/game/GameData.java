package xyz.jinenze.wuhumc.game;

import net.minecraft.server.level.ServerPlayer;
import xyz.jinenze.wuhumc.action.ActionList;
import xyz.jinenze.wuhumc.action.Event;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.ServerActionContext;

public record GameData(Event onReadyEvent, ActionList<ServerActionContext> gameStartAction,
                       EventListener<ServerPlayer> notReadyListener) {
}
