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
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.storage.LevelData;
import org.spongepowered.asm.mixin.Mixin;
import xyz.jinenze.wuhumc.Wuhumc;

@Mixin(WeightedPressurePlateBlock.class)
public abstract class WeightedPressurePlateBlockMixin extends BasePressurePlateBlock {
    public WeightedPressurePlateBlockMixin(Properties properties, BlockSetType blockSetType) {
        super(properties, blockSetType);
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean bl) {
        if (entity instanceof ServerPlayer player) {
            if (blockState.getBlock().equals(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE)) {
                if (player.getRespawnConfig() == null) {
                    player.setRespawnPosition(new ServerPlayer.RespawnConfig(new LevelData.RespawnData(new GlobalPos(level.dimension(), blockPos.above(1)), 0, 0), true), false);
                    player.sendSystemMessage(Component.translatable("title.wuhumc.spawn_point"), true);
                    player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_PLING, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1f, 0.5f, 0));
                    return;
                }
                BlockPos oldPos = player.getRespawnConfig().respawnData().pos();
                if (!oldPos.equals(blockPos.above(1))) {
                    player.setRespawnPosition(new ServerPlayer.RespawnConfig(new LevelData.RespawnData(new GlobalPos(level.dimension(), blockPos.above(1)), 0, 0), true), false);
                    player.sendSystemMessage(Component.translatable("title.wuhumc.spawn_point"), true);
                    player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_PLING, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1f, 0.5f, 0));
                    Wuhumc.LOGGER.info(oldPos.toShortString());
                    Wuhumc.LOGGER.info(blockPos.toShortString());
                }
            }
        }
    }
}
