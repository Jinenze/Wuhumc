package xyz.jinenze.wuhumc.client.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.jinenze.wuhumc.init.ModItems;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow
    public abstract ItemStack getStack();

    @Inject(method = "canTakeItems", at = @At("HEAD"), cancellable = true)
    private void canTakeItemsInject(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> cir) {
        var itemStack = this.getStack();
        cir.setReturnValue(playerEntity.isCreative() || !(itemStack.getItem().equals(ModItems.READY_ITEM) || itemStack.getItem().equals(ModItems.NOT_READY_ITEM)));
    }
}
