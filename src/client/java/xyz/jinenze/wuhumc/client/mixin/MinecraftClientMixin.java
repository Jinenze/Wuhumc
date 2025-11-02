package xyz.jinenze.wuhumc.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jinenze.wuhumc.init.ModItems;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public ClientPlayerEntity player;
    @Final
    @Shadow
    public GameOptions options;

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void handleInputEventsInject(CallbackInfo ci) {
        if (player.getInventory().getSelectedStack().getItem().equals(ModItems.READY_ITEM) || player.getInventory().getSelectedStack().getItem().equals(ModItems.READY_ITEM)) {
            ((KeyBindingInvoker) this.options.dropKey).wuhumc$reset();
        }
    }
}
