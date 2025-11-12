package xyz.jinenze.wuhumc.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(KeyMapping.class)
public interface KeyBindingInvoker {
    @Invoker("release")
    void wuhumc$reset();
}
