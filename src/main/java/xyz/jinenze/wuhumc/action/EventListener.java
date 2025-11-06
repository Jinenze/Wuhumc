package xyz.jinenze.wuhumc.action;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.jinenze.wuhumc.init.ModServerEvents;

import java.util.function.Consumer;

public record EventListener<T extends PlayerEntity>(ModServerEvents event, Consumer<T> action) {
}
