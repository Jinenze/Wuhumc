package xyz.jinenze.wuhumc.util;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.jinenze.wuhumc.init.ModItems;

public class PlayerItemUtil {
    public static void removeReadyItemFromPlayer(ServerPlayerEntity player) {
        for (int index = 0; index < player.getInventory().size(); ++index) {
            var itemStack = player.getInventory().getStack(index);
            var item = itemStack.getItem();
            if (item.equals(ModItems.NOT_READY_ITEM) || item.equals(ModItems.READY_ITEM)) {
                player.getInventory().removeStack(index);
            }
        }
    }
}
