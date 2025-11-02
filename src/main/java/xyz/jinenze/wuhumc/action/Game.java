package xyz.jinenze.wuhumc.action;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.jinenze.wuhumc.init.ModServerEvents;

public record Game(ModServerEvents onReadyEvent, ModServerEvents gameStartEvent,
                   EventListener<ServerPlayerEntity> notReadyListener,
                   EventListener<ServerPlayerEntity> gameStartListener) {
}
