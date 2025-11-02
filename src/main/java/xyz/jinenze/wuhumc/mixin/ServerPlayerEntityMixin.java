package xyz.jinenze.wuhumc.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jinenze.wuhumc.action.ActionProcessor;
import xyz.jinenze.wuhumc.action.Game;
import xyz.jinenze.wuhumc.action.ServerMixinGetter;
import xyz.jinenze.wuhumc.init.ModServerEvents;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerMixinGetter {
    @Unique
    private final ActionProcessor<ServerPlayerEntity> processor = new ActionProcessor<>((ServerPlayerEntity) (Object) this);

    @Unique
    private Game currentGame;

    @Override
    public void wuhumc$setCurrentGame(Game game) {
        this.currentGame = game;
    }

    @Override
    public Game wuhumc$getCurrentGame() {
        return currentGame;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void playerTickInject(CallbackInfo ci) {
        processor.tick();
    }

    @Override
    public ActionProcessor<ServerPlayerEntity> wuhumc$getProcessor() {
        return processor;
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
        processor.emitEvent(ModServerEvents.FALL_VOID);
    }
}
