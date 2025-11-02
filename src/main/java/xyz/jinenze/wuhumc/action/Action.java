package xyz.jinenze.wuhumc.action;

import net.minecraft.entity.player.PlayerEntity;

public interface Action<T extends PlayerEntity> {
    boolean run(T player, ActionProcessor<T>.ActionsHandler handler);
}
