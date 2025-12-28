package xyz.jinenze.wuhumc.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.game.GameSession;
import xyz.jinenze.wuhumc.init.ModGames;
import xyz.jinenze.wuhumc.init.ModItems;

public class Util {
//        public static void removeItemsFromPlayer(ServerPlayer player, List<Item> items) {
//        for (int index = 0; index < player.getInventory().getContainerSize(); ++index) {
//            var itemStack = player.getInventory().getItem(index);
//            for (var item : items) {
//                if (itemStack.getItem().equals(item)) {
//                    player.getInventory().removeItemNoUpdate(index);
//                }
//            }
//        }
//    }

//        public static void ejectPlayer(ServerPlayer player) {
//        resetPlayerPosition(player);
//        player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), new Vec3(ProcessorManager.get(player).getCurrentGame().gameStartPlayerEjectDirection ? 1 : -1, 0.5, 0)));
//        ProcessorManager.get(player).getCurrentGame().gameStartPlayerEjectDirection = !ProcessorManager.get(player).getCurrentGame().gameStartPlayerEjectDirection;
//    }

    public static void placeReadyItemToIndexZero(ServerPlayer player) {
        int index = player.getInventory().getFreeSlot();
        if (!(index == -1)) {
            player.getInventory().setItem(index, player.getInventory().getItem(0).copy());
        }
        player.getInventory().setItem(0, new ItemStack(ModItems.READY_ITEM.getItem()));
        player.getInventory().setSelectedSlot(0);
        player.connection.send(new ClientboundSetHeldSlotPacket(0));
    }

    public static void removeCurrentContainerItemsFromPlayer(ServerPlayer player) {
        player.containerMenu.setCarried(ItemStack.EMPTY);
        player.containerMenu.slots.forEach(slot -> slot.set(ItemStack.EMPTY));
    }

    public static void removeInventoryItemsFromPlayer(ServerPlayer player) {
        player.containerMenu.setCarried(ItemStack.EMPTY);
        player.inventoryMenu.slots.forEach(slot -> slot.set(ItemStack.EMPTY));
    }

    public static void removeItemsFromGround(ServerLevel world) {
        world.getAllEntities().forEach(entity -> {
            if (entity instanceof ItemEntity) {
                entity.discard();
            }
        });
    }

    public static boolean isReadyItems(Item item) {
        return item.equals(ModItems.NOT_READY_ITEM.getItem()) || item.equals(ModItems.READY_ITEM.getItem());
    }

    public static boolean isPlayerInGame(ServerPlayer player) {
        return ProcessorManager.get(player).getGameSession() != ModGames.NULL;
    }

    public static void setOverworldSpawnPoint(ServerPlayer player, BlockPos blockPos) {
        player.setRespawnPosition(new ServerPlayer.RespawnConfig(new LevelData.RespawnData(new GlobalPos(Level.OVERWORLD, blockPos), 0, 0), true), false);
    }

    public static void teleportTo(ServerPlayer player, Vec3 pos) {
        player.connection.teleport(new PositionMoveRotation(pos, Vec3.ZERO, 0.0F, 0.0F), Relative.union(Relative.DELTA, Relative.ROTATION));
    }

    public static void resetPlayerPosition(ServerPlayer player) {
        teleportTo(player, player.getRespawnConfig().respawnData().pos().getBottomCenter());
    }

    public static void addScore(ServerPlayer player) {
        ProcessorManager.get(player).getGameSession().addScore(player);
    }

    public static void addScore(ServerPlayer player, int score) {
        if (score == 0) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_add_score_failed")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            return;
        } else if (score > 0) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_add_score")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        } else {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_minus_score")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        ProcessorManager.get(player).addScore(score);
    }

    public static void setPlayerGameSession(ServerPlayer player, GameSession gameSession) {
        if (gameSession.isRunning()) {
            player.setGameMode(GameType.SPECTATOR);
            ProcessorManager.get(player).setCurrentGame(gameSession);
        } else {
            ProcessorManager.get(player).emitListener(gameSession.getGameData().notReadyListener());
            ProcessorManager.get(player).setCurrentGame(gameSession);
            Util.placeReadyItemToIndexZero(player);
        }
    }
}
