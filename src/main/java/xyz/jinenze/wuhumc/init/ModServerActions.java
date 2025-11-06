package xyz.jinenze.wuhumc.init;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.*;

import java.util.Iterator;

public class ModServerActions {
    public static final Actions<ServerPlayerEntity> NULL = Actions.<ServerPlayerEntity>getBuilder().action((player, handler) -> true).build();

    private static boolean countdown(ServerPlayerEntity player, String key) {
        for (ServerPlayerEntity player1 : player.getEntityWorld().getServer().getPlayerManager().getPlayerList()) {
            if (ProcessorManager.getInstance().get(player1).event(ProcessorManager.getInstance().get(player).getCurrentGame().onReadyEvent())) {
                player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.wuhumc.game_cancel")));
                return true;
            }
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable(key)));
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        return false;
    }

    public static final Actions<ServerPlayerEntity> GAME_COUNTDOWN = Actions.<ServerPlayerEntity>getBuilder().action((player, handler) -> {
        player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.wuhumc.game_countdown_5")));
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        return false;
    }
    ).wait(20).action((player, handler) -> countdown(player, "title.wuhumc.game_countdown_4")
    ).wait(20).action((player, handler) -> countdown(player, "title.wuhumc.game_countdown_3")
    ).wait(20).action((player, handler) -> countdown(player, "title.wuhumc.game_countdown_2")
    ).wait(20).action((player, handler) -> countdown(player, "title.wuhumc.game_countdown_1")
    ).wait(20).action((player, handler) -> {
        countdown(player, "title.wuhumc.game_countdown_end");
        for (ServerPlayerEntity player1 : player.getEntityWorld().getServer().getPlayerManager().getPlayerList()) {
            ProcessorManager.getInstance().get(player1).event(ProcessorManager.getInstance().get(player).getCurrentGame().gameStartEvent());
        }
        return true;
    }).build();

    public static final Actions<ServerPlayerEntity> RESPAWN_FLY = Actions.<ServerPlayerEntity>getBuilder().action((player, handler) -> {
        if (player.getRespawn() != null && Wuhumc.config.isRespawnFlyEnabled()) {
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player.getId(), new Vec3d(0, 10, 0)));
            return false;
        }
        return true;
    }).wait(60).action((player, handler) -> {
        if (player.getRespawn() != null) {
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player.getId(), new Vec3d(0, 0, 0)));
            player.teleport(player.getRespawn().respawnData().getPos().toBottomCenterPos().getX(),player.getRespawn().respawnData().getPos().toBottomCenterPos().getY(), player.getRespawn().respawnData().getPos().toBottomCenterPos().getZ(), false);
        }
        return true;
    }).build();

    public static final ActionProvider<ServerPlayerEntity> dumbActions = () -> new Iterator<>() {
        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Action<ServerPlayerEntity> next() {
            return (player, handler) -> {
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 1f, 0.5f);
                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 1f, 0.5f, 9));
                handler.setDelay(1);
                return false;
            };
        }
    };

    public static final Actions<ServerPlayerEntity> test = Actions.<ServerPlayerEntity>getBuilder().action((player, handler) -> {
        player.getEntityWorld().getServer().getCommandManager().parseAndExecute(player.getCommandSource(), "/me LLLLLL");
        return true;
    }).build();

    public static void register() {
    }
}
