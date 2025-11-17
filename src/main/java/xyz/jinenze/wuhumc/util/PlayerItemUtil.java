package xyz.jinenze.wuhumc.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import xyz.jinenze.wuhumc.init.ModItems;

import java.util.List;

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
}
