package xyz.jinenze.wuhumc.client.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyMapping.class)
public interface KeyMappingInvoker {
    @Invoker("release")
    void wuhumc$reset();
}
