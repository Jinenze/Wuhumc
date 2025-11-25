package xyz.jinenze.wuhumc.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import xyz.jinenze.wuhumc.action.PlayerProcessor;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.init.ModServerActions;
import xyz.jinenze.wuhumc.init.ModServerEvents;
import xyz.jinenze.wuhumc.util.ServerPlayerMixinGetter;

import java.util.List;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin extends Player implements ServerPlayerMixinGetter {
    @Unique
    private final PlayerProcessor<ServerPlayer> processor = ProcessorManager.createOrRefresh((ServerPlayer) (Object) this);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initInject(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, ClientInformation clientInformation, CallbackInfo ci) {
        processor.emitActions(ModServerActions.RESPAWN_FLY);
    }

    @Inject(method = "triggerRecipeCrafted", at = @At("TAIL"))
    private void craftedInject(RecipeHolder<?> recipeHolder, List<ItemStack> list, CallbackInfo ci) {
        if (recipeHolder.id().equals(ResourceKey.create(Registries.RECIPE, ResourceLocation.withDefaultNamespace("diamond_axe")))) {
            processor.event(ModServerEvents.PLAYER_CRAFTED_DIAMOND_AXE);
        }
    }

    @Inject(method = "onItemPickup", at = @At("TAIL"))
    public void onItemPickupInject(ItemEntity itemEntity, CallbackInfo ci) {
        super.onItemPickup(itemEntity);
        if (!level().isClientSide() && itemEntity.getItem().getItem().equals(Items.DIAMOND) && itemEntity.getOwner() instanceof ServerPlayer serverOwner && !serverOwner.equals(this)) {
            ProcessorManager.get(serverOwner).event(ModServerEvents.PLAYER_ANOTHER_PLAYER_PICKUP_DIAMOND);
        }
    }

    @Override
    public PlayerProcessor<ServerPlayer> wuhumc$getProcessor() {
        return processor;
    }

    @Shadow
    public abstract @NotNull ServerLevel level();

    @Override
    public void lavaHurt() {
        this.kill(this.level());
    }

    @Override
    protected void onBelowWorld() {
        ProcessorManager.get((ServerPlayer) (Object) this).event(ModServerEvents.PLAYER_FALL_VOID);
        this.kill(this.level());
    }

    @Override
    public void setShiftKeyDown(boolean bl) {
        super.setShiftKeyDown(bl);
        if (bl) {
            processor.event(ModServerEvents.PLAYER_SHIFT_DOWN);
        }
    }

    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }
}
