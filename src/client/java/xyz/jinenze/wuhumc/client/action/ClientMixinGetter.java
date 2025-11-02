package xyz.jinenze.wuhumc.client.action;

import net.minecraft.client.network.ClientPlayerEntity;
import xyz.jinenze.wuhumc.action.ActionProcessor;

public interface ClientMixinGetter {
    ActionProcessor<ClientPlayerEntity> wuhumc$getProcessor();
}
