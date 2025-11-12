package xyz.jinenze.wuhumc.util;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.jinenze.wuhumc.action.PlayerProcessor;

public interface ServerPlayerMixinGetter {
    PlayerProcessor<ServerPlayerEntity> wuhumc$getProcessor();
}