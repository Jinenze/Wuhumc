package xyz.jinenze.wuhumc.util;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;

public class InventorySnapshot {
    private final Inventory inventorySnapshot;

    public InventorySnapshot(ServerPlayer player) {
        var snapshot = new Inventory(player, new EntityEquipment());
        snapshot.replaceWith(player.getInventory());
        snapshot.add(player.containerMenu.getCarried());
        player.inventoryMenu.getInputGridSlots().forEach(slot -> snapshot.add(slot.getItem()));
        this.inventorySnapshot = snapshot;
    }

    public Inventory getInventory() {
        return inventorySnapshot;
    }
}
