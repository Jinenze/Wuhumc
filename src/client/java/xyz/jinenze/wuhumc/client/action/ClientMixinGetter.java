package xyz.jinenze.wuhumc.client.action;

import net.minecraft.client.network.ClientPlayerEntity;
import xyz.jinenze.wuhumc.action.PlayerProcessor;

public interface ClientMixinGetter {
    PlayerProcessor<ClientPlayerEntity> wuhumc$getProcessor();
}
