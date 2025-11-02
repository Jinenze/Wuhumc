package xyz.jinenze.wuhumc.action;

import net.minecraft.entity.player.PlayerEntity;
import xyz.jinenze.wuhumc.init.ModServerEvents;

public record EventListener<T extends PlayerEntity>(ModServerEvents event, ActionProvider<T> action) {
}
