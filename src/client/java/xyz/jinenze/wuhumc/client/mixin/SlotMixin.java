package xyz.jinenze.wuhumc.client.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.jinenze.wuhumc.init.ModItems;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow
    public abstract ItemStack getItem();

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void canTakeItemsInject(Player playerEntity, CallbackInfoReturnable<Boolean> cir) {
        var itemStack = this.getItem();
        cir.setReturnValue(playerEntity.isCreative() || !(itemStack.getItem().equals(ModItems.READY_ITEM) || itemStack.getItem().equals(ModItems.NOT_READY_ITEM)));
    }
}
