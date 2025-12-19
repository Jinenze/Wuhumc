package xyz.jinenze.wuhumc.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.config.ServerConfig;
import xyz.jinenze.wuhumc.init.ModItems;

public class PlayerUtil {
    //    public static void removeItemsFromPlayer(ServerPlayer player, List<Item> items) {
//        for (int index = 0; index < player.getInventory().getContainerSize(); ++index) {
//            var itemStack = player.getInventory().getItem(index);
//            for (var item : items) {
//                if (itemStack.getItem().equals(item)) {
//                    player.getInventory().removeItemNoUpdate(index);
//                }
//            }
//        }
//    }
    public static void placeReadyItemToPlayer(ServerPlayer player) {
        int index = player.getInventory().getFreeSlot();
        if (!(index == -1)) {
            player.getInventory().setItem(index, player.getInventory().getItem(0).copy());
        }
        player.getInventory().setItem(0, new ItemStack(ModItems.READY_ITEM.getItem()));
        player.getInventory().setSelectedSlot(0);
        player.connection.send(new ClientboundSetHeldSlotPacket(0));
    }

    public static void removeReadyItemFromPlayer(ServerPlayer player) {
//        for (int index = 0; index < player.getInventory().getContainerSize(); ++index) {
//            var itemStack = player.getInventory().getItem(index);
//            var item = itemStack.getItem();
//            if (isInReadyItems(item)) {
//                player.getInventory().removeItemNoUpdate(index);
//            }
//        }
        player.getInventory().removeItemNoUpdate(0);
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

    public static boolean isInReadyItems(Item item) {
        return item.equals(ModItems.NOT_READY_ITEM.getItem()) || item.equals(ModItems.READY_ITEM.getItem());
    }

    public static void resetPlayerPosition(ServerPlayer player) {
        var pos = player.getRespawnConfig().respawnData().pos().getBottomCenter();
        player.teleportTo(pos.x(), pos.y(), pos.z());
    }

    public static void ejectPlayer(ServerPlayer player) {
        resetPlayerPosition(player);
        player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), new Vec3(ProcessorManager.get(player).getCurrentGame().gameStartPlayerEjectDirection ? 1 : -1, 0.5, 0)));
        ProcessorManager.get(player).getCurrentGame().gameStartPlayerEjectDirection = !ProcessorManager.get(player).getCurrentGame().gameStartPlayerEjectDirection;
    }

    public static void setSpawnPoint(ServerPlayer player, ServerConfig.GamePosition config) {
        player.setRespawnPosition(new ServerPlayer.RespawnConfig(new LevelData.RespawnData(new GlobalPos(Level.OVERWORLD, new BlockPos(config.x, config.y, config.z)), 0, 0), true), false);
    }

    public static void addScoreAndShowMessage(ServerPlayer player) {
        ProcessorManager.get(player).getCurrentGame().addScore(player);
    }

    public static void teleportTo(ServerPlayer player, Vec3 pos) {
        player.connection.teleport(new PositionMoveRotation(pos, Vec3.ZERO, 0.0F, 0.0F), Relative.union(Relative.DELTA, Relative.ROTATION));
    }
}
