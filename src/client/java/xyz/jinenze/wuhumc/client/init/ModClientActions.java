package xyz.jinenze.wuhumc.client.init;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundEvents;
import xyz.jinenze.wuhumc.action.Actions;

public class ModClientActions {
    public static final Actions<ClientPlayerEntity> respawnMusic = Actions.<ClientPlayerEntity>getBuilder().wait(4).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 0.6f, 0.7937005259840998f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 0.6f, 1.0594630943592953f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 0.6f, 1.2599210498948732f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 0.9438743126816935f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 0.9438743126816935f);
        return false;
    }).wait(4).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 1.4142135623730951f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 1.4142135623730951f);
        return false;
    }).wait(4).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 1.2599210498948732f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 1.2599210498948732f);
        return false;
    }).wait(4).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 0.6f, 0.7937005259840998f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 0.6f, 0.9438743126816935f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 0.6f, 1.2599210498948732f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 0.9438743126816935f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 0.9438743126816935f);
        return false;
    }).wait(8).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 1.2599210498948732f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 1.2599210498948732f);
        return false;
    }).wait(2).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 1.4142135623730951f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 1.4142135623730951f);
        return false;
    }).wait(2).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 1.5874010519681994f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 1.5874010519681994f);
        return false;
    }).wait(2).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 1.4142135623730951f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 1.4142135623730951f);
        return false;
    }).wait(2).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 1.2599210498948732f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 1.2599210498948732f);
        return false;
    }).wait(2).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 1.4142135623730951f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 1.4142135623730951f);
        return false;
    }).wait(2).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 0.6f, 0.8408964152537145f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 0.6f, 1.0594630943592953f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 0.6f, 1.2599210498948732f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 0.9438743126816935f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 0.9438743126816935f);
        return false;
    }).wait(4).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 1.4142135623730951f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 1.4142135623730951f);
        return false;
    }).wait(4).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 1.2599210498948732f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 1.2599210498948732f);
        return false;
    }).wait(4).action((player, handler) -> {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 0.6f, 0.7937005259840998f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 0.6f, 0.9438743126816935f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 0.6f, 1.2599210498948732f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f, 0.9438743126816935f);
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6f, 0.9438743126816935f);
        return false;
    }).build();

    public static void register() {
    }
}
