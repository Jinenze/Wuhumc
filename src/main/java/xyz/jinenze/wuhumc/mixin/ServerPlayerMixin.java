package xyz.jinenze.wuhumc.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.action.ServerPlayerProcessor;
import xyz.jinenze.wuhumc.init.ModEvents;
import xyz.jinenze.wuhumc.init.ModServerActions;
import xyz.jinenze.wuhumc.util.ServerPlayerMixinGetter;

import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements ServerPlayerMixinGetter {
    @Unique
    private final ServerPlayerProcessor processor = ProcessorManager.createOrRefresh((ServerPlayer) (Object) this);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initInject(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, ClientInformation clientInformation, CallbackInfo ci) {
        processor.emitActions(ModServerActions.RESPAWN_FLY);
    }

    @Inject(method = "triggerRecipeCrafted", at = @At("TAIL"))
    private void craftedInject(RecipeHolder<?> recipeHolder, List<ItemStack> list, CallbackInfo ci) {
        processor.emitEventToAll(new ModEvents.CraftEvent(recipeHolder.id()));
    }

    @Inject(method = "onItemPickup", at = @At("TAIL"))
    private void onItemPickupInject(ItemEntity itemEntity, CallbackInfo ci) {
        super.onItemPickup(itemEntity);
        if (itemEntity.getOwner() instanceof ServerPlayer owner && !owner.equals(this) && itemEntity.getItem().getItem().equals(Items.DIAMOND)) {
            ProcessorManager.get(owner).emitEventToAll(ModEvents.PLAYER_ANOTHER_PLAYER_PICKUP_DIAMOND);
        }
    }

    @Override
    public ServerPlayerProcessor wuhumc$getProcessor() {
        return processor;
    }

    @Shadow
    public abstract @NotNull ServerLevel level();

    @Inject(method = "die", at = @At("TAIL"))
    private void dieInject(DamageSource damageSource, CallbackInfo ci) {
        if (damageSource.getEntity() instanceof ServerPlayer source && !source.equals(this)) {
            ProcessorManager.get(source).emitEventToAll(ModEvents.PLAYER_KILLED_ANOTHER_PLAYER);
        }
    }

    @Override
    protected void onBelowWorld() {
//        processor.emitEventToAll(ModEvents.PLAYER_FALL_VOID);
        this.kill(this.level());
    }

    @Override
    public void setShiftKeyDown(boolean bl) {
        super.setShiftKeyDown(bl);
        if (bl) {
            processor.emitEventToAll(ModEvents.PLAYER_SNEAK);
        }
    }

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void completeUsingItemInject(CallbackInfo ci) {
        if (!this.useItem.isEmpty() && this.isUsingItem()) {
            if (this.useItem.get(DataComponents.FOOD) != null) {
                processor.emitEventToAll(new ModEvents.EatEvent(this.useItem.getItem()));
            }
        }
    }

    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }
}
