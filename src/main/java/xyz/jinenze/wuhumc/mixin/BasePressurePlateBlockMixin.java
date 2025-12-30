package xyz.jinenze.wuhumc.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.init.ModEvents;

@Mixin(BasePressurePlateBlock.class)
public abstract class BasePressurePlateBlockMixin {
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    protected void entityInsideInject(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean bl, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player) {
            if (blockState.getBlock().equals(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE)) {
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
                ci.cancel();
            } else if (blockState.getBlock().equals(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE) && blockState.getValue(BlockStateProperties.POWER) == 0) {
                player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), new Vec3((-Math.sin(Math.toRadians(player.getYRot()))) * Wuhumc.config.heavy_weight_pressure_plate_force_factor, 0.5, Math.cos(Math.toRadians(player.getYRot())) * Wuhumc.config.heavy_weight_pressure_plate_force_factor)));
                player.connection.send(new ClientboundSoundPacket(new Holder.Direct<>(SoundEvents.BAT_TAKEOFF), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1f, 0.5f, 0));
            }
        } else if (entity instanceof Boat boat) {
            if (blockState.getBlock().equals(Blocks.OAK_PRESSURE_PLATE) && boat.getFirstPassenger() != null && boat.getFirstPassenger() instanceof ServerPlayer player) {
                ProcessorManager.get(player).emitEventToAll(ModEvents.PLAYER_TRIGGER_OAK_PRESSURE_PLATE);
                ci.cancel();
            }
        }
    }
}
