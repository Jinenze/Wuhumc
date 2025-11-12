package xyz.jinenze.wuhumc.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

@Mixin(ServerPlayer.class)
abstract class ServerPlayerEntityMixin extends Player implements ServerPlayerMixinGetter {
    @Unique
    private final PlayerProcessor<ServerPlayer> processor = ProcessorManager.createOrRefresh((ServerPlayer) (Object) this);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initInject(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, ClientInformation clientInformation, CallbackInfo ci) {
        processor.emitActions(ModServerActions.RESPAWN_FLY);
    }

    @Override
    public PlayerProcessor<ServerPlayer> wuhumc$getProcessor() {
        return processor;
    }

    @Shadow
    public abstract ServerLevel level();

    public ServerPlayerEntityMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public void lavaHurt() {
        this.kill(this.level());
    }

    @Override
    protected void onBelowWorld() {
        ProcessorManager.get((ServerPlayer) (Object) this).event(ModServerEvents.PLAYER_FALL_VOID);
        this.kill(this.level());
    }
}
