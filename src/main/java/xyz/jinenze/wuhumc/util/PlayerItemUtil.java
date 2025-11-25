package xyz.jinenze.wuhumc.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import xyz.jinenze.wuhumc.init.ModItems;

import java.util.List;
import java.util.function.Predicate;

public class PlayerItemUtil {
    public static void removeItemsFromPlayer(ServerPlayer player, List<Item> items) {
        for (int index = 0; index < player.getInventory().getContainerSize(); ++index) {
            var itemStack = player.getInventory().getItem(index);
            for (var item : items) {
                if (itemStack.getItem().equals(item)) {
                    player.getInventory().removeItemNoUpdate(index);
                }
            }
        }
    }

    public static void removeReadyItemFromPlayer(ServerPlayer player) {
        for (int index = 0; index < player.getInventory().getContainerSize(); ++index) {
            var itemStack = player.getInventory().getItem(index);
            var item = itemStack.getItem();
            if (item.equals(ModItems.NOT_READY_ITEM) || item.equals(ModItems.READY_ITEM)) {
                player.getInventory().removeItemNoUpdate(index);
            }
        }
    }

    public static void deleteItenListFromGround(ServerLevel world, List<Item> items) {
        world.getAllEntities().forEach(entity -> {
            if (entity instanceof ItemEntity itemEntity && items.contains(itemEntity.getItem().getItem())) {
                entity.discard();
            }
        });
    }
}
