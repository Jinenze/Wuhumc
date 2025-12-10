package xyz.jinenze.wuhumc.util;


import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;

public class InventorySnapshot {
    private final Inventory inventorySnapshot;

    public InventorySnapshot(Inventory inventory) {
        var snapshot = new Inventory(inventory.player, new EntityEquipment());
        snapshot.replaceWith(inventory);
        this.inventorySnapshot = snapshot;
    }

    public Inventory getInventory() {
        return inventorySnapshot;
    }
}
