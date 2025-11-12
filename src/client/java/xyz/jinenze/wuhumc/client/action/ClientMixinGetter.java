package xyz.jinenze.wuhumc.client.action;

import net.minecraft.client.player.LocalPlayer;
import xyz.jinenze.wuhumc.action.PlayerProcessor;

public interface ClientMixinGetter {
    PlayerProcessor<LocalPlayer> wuhumc$getProcessor();
}
