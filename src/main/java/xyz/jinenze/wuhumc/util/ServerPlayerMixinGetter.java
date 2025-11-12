package xyz.jinenze.wuhumc.util;

import net.minecraft.server.level.ServerPlayer;
import xyz.jinenze.wuhumc.action.PlayerProcessor;

public interface ServerPlayerMixinGetter {
    PlayerProcessor<ServerPlayer> wuhumc$getProcessor();
}