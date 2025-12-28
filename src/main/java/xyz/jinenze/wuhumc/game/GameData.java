package xyz.jinenze.wuhumc.game;

import net.minecraft.server.level.ServerPlayer;
import xyz.jinenze.wuhumc.action.ActionSupplier;
import xyz.jinenze.wuhumc.action.Event;
import xyz.jinenze.wuhumc.action.EventListener;
import xyz.jinenze.wuhumc.action.ServerActionContext;

public record GameData(Event onReadyEvent, ActionSupplier<ServerActionContext> gameStartAction,
                       EventListener<ServerPlayer> notReadyListener) {
}
