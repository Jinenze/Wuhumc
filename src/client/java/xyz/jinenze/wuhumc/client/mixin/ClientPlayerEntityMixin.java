package xyz.jinenze.wuhumc.client.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jinenze.wuhumc.action.ActionProcessor;
import xyz.jinenze.wuhumc.client.action.ClientMixinGetter;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin implements ClientMixinGetter {
    @Unique
    private final ActionProcessor<ClientPlayerEntity> processor = new ActionProcessor<>((ClientPlayerEntity) (Object) this);

    @Inject(method = "tick", at = @At("TAIL"))
    private void playerTickInject(CallbackInfo ci) {
        processor.tick();
    }

    @Override
    public ActionProcessor<ClientPlayerEntity> wuhumc$getProcessor() {
        return processor;
    }
}
