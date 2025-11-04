package xyz.jinenze.wuhumc.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jinenze.wuhumc.action.Game;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.init.ModServerEvents;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Inject(method = "tick", at = @At("TAIL"))
    private void playerTickInject(CallbackInfo ci) {
        ProcessorManager.getInstance().getProcessor((ServerPlayerEntity)(Object)this).tick();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initInject(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions, CallbackInfo ci) {
        ProcessorManager.getInstance().setPlayer((ServerPlayerEntity) (Object) this);
    }

    @Shadow
    public abstract ServerWorld getEntityWorld();

    public ServerPlayerEntityMixin(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Override
    public void setOnFireFromLava() {
        this.kill(this.getEntityWorld());
    }

    @Override
    protected void tickInVoid() {
        this.kill(this.getEntityWorld());
        ProcessorManager.getInstance().getProcessor((ServerPlayerEntity)(Object)this).emitEvent(ModServerEvents.FALL_VOID);
    }
}
