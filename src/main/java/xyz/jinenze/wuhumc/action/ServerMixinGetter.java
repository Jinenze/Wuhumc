package xyz.jinenze.wuhumc.action;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ServerMixinGetter {
    ActionProcessor<ServerPlayerEntity> wuhumc$getProcessor();
}