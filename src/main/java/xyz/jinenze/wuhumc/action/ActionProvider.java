package xyz.jinenze.wuhumc.action;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Iterator;

public interface ActionProvider<T extends PlayerEntity> {
    Iterator<Action<T>> iterator();
}
