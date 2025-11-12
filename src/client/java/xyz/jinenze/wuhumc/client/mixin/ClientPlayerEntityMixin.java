package xyz.jinenze.wuhumc.client.mixin;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jinenze.wuhumc.action.PlayerProcessor;
import xyz.jinenze.wuhumc.client.WuhumcClient;
import xyz.jinenze.wuhumc.client.action.ClientMixinGetter;
import xyz.jinenze.wuhumc.client.init.ModClientActions;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin implements ClientMixinGetter {
    @Unique
    private static final PlayerProcessor<LocalPlayer> processor = new PlayerProcessor<>();

    @Inject(method = "tick", at = @At("TAIL"))
    private void playerTickInject(CallbackInfo ci) {
        processor.tick();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initInject(Minecraft minecraft, ClientLevel clientLevel, ClientPacketListener clientPacketListener, StatsCounter statsCounter, ClientRecipeBook clientRecipeBook, Input input, boolean bl, CallbackInfo ci) {
        processor.setPlayer((LocalPlayer) (Object) this);
        if (WuhumcClient.config.respawnMusic) {
            processor.emitActions(ModClientActions.respawnMusic);
        }
    }

    @Override
    public PlayerProcessor<LocalPlayer> wuhumc$getProcessor() {
        return processor;
    }
}
