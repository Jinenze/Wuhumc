package xyz.jinenze.wuhumc.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jinenze.wuhumc.Wuhumc;

@Mixin(BasePressurePlateBlock.class)
public abstract class BasePressurePlateBlockMixin {
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    protected void entityInsideInject(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean bl, CallbackInfo ci) {
        if (blockState.getBlock().equals(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE)) {
            if (entity instanceof ServerPlayer player) {
                if (player.getRespawnConfig() == null) {
                    player.setRespawnPosition(new ServerPlayer.RespawnConfig(new LevelData.RespawnData(new GlobalPos(level.dimension(), blockPos.above(1)), 0, 0), true), false);
                    player.sendSystemMessage(Component.translatable("message.wuhumc.spawn_point"), true);
                    player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_PLING, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1f, 0.5f, 0));
                    ci.cancel();
                }
                BlockPos oldPos = player.getRespawnConfig().respawnData().pos();
                if (oldPos.equals(blockPos.above(1))) {
                    ci.cancel();
                }
                player.setRespawnPosition(new ServerPlayer.RespawnConfig(new LevelData.RespawnData(new GlobalPos(level.dimension(), blockPos.above(1)), 0, 0), true), false);
                player.sendSystemMessage(Component.translatable("message.wuhumc.spawn_point"), true);
                player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_PLING, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1f, 0.5f, 0));
                Wuhumc.LOGGER.info(oldPos.toShortString());
                Wuhumc.LOGGER.info(blockPos.toShortString());
            }
            ci.cancel();
        }
    }
}
