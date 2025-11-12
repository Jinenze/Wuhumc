package xyz.jinenze.wuhumc.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jinenze.wuhumc.init.ModItems;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public LocalPlayer player;
    @Final
    @Shadow
    public Options options;

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void handleInputEventsInject(CallbackInfo ci) {
        if (player.getInventory().getSelectedItem().getItem().equals(ModItems.READY_ITEM) || player.getInventory().getSelectedItem().getItem().equals(ModItems.NOT_READY_ITEM)) {
            ((KeyBindingInvoker) this.options.keyDrop).wuhumc$reset();
            ((KeyBindingInvoker) this.options.keySwapOffhand).wuhumc$reset();
        }
    }
}
