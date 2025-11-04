package xyz.jinenze.wuhumc.init;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.*;

import java.util.Iterator;

public class ModServerActions {
    public static final Actions<ServerPlayerEntity> NULL = Actions.<ServerPlayerEntity>getBuilder().action((player, handler) -> false).build();

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
        return false;
    }).build();

    public static final Actions<ServerPlayerEntity> fallVoid = Actions.<ServerPlayerEntity>getBuilder().action((player, handler) -> {
        Wuhumc.LOGGER.info("LLLLLLLLLLLLLl");
        return false;
    }).build();

    public static Actions<ServerPlayerEntity> getSendListenerAction(EventListener<ServerPlayerEntity> listener) {
        return Actions.<ServerPlayerEntity>getBuilder().action((player, handler) -> {
            ProcessorManager.getInstance().getProcessor(player).listen(listener);
            return true;
        }).build();
    }

    public static void register() {
    }
}
