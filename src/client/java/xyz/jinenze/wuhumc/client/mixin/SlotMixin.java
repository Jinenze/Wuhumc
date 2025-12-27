package xyz.jinenze.wuhumc.client.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.jinenze.wuhumc.util.Util;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow
    public abstract ItemStack getItem();

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void mayPickupInject(Player playerEntity, CallbackInfoReturnable<Boolean> cir) {
        if (Util.isReadyItems(getItem().getItem())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void mayPlaceInject(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (Util.isReadyItems(itemStack.getItem())) {
            cir.setReturnValue(false);
        }
    }
}
