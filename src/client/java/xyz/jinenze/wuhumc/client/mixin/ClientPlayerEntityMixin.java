package xyz.jinenze.wuhumc.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jinenze.wuhumc.action.ActionProcessor;
import xyz.jinenze.wuhumc.client.action.ClientMixinGetter;
import xyz.jinenze.wuhumc.client.init.ModClientActions;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin implements ClientMixinGetter {
    @Unique
    private static final ActionProcessor<ClientPlayerEntity> processor = new ActionProcessor<>();

    @Inject(method = "tick", at = @At("TAIL"))
    private void playerTickInject(CallbackInfo ci) {
        processor.tick();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initInject(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, PlayerInput lastPlayerInput, boolean lastSprinting, CallbackInfo ci) {
        processor.setPlayer((ClientPlayerEntity) (Object) this);
        processor.emitActions(ModClientActions.respawnMusic);
    }

    @Override
    public ActionProcessor<ClientPlayerEntity> wuhumc$getProcessor() {
        return processor;
    }
}
