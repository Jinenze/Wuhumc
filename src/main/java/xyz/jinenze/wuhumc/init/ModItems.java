package xyz.jinenze.wuhumc.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.item.NotReadyItem;
import xyz.jinenze.wuhumc.item.ReadyItem;

import java.util.function.Function;

public enum ModItems {
    READY_ITEM(registerItem("ready_item", ReadyItem::new)),
    NOT_READY_ITEM(registerItem("not_ready_item", NotReadyItem::new)),
    ;
    private final Item item;

    ModItems(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public static final ResourceKey<CreativeModeTab> CUSTOM_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Wuhumc.MOD_ID, "item_group"));

    public static final CreativeModeTab CUSTOM_TAB = FabricItemGroup.builder()
            .icon(() -> new ItemStack(READY_ITEM.getItem()))
            .title(Component.translatable("itemGroup.wuhumc.wuhumc"))
            .build();

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CUSTOM_TAB_KEY, CUSTOM_TAB);
        ItemGroupEvents.modifyEntriesEvent(CUSTOM_TAB_KEY).register(itemGroup -> {
            for (var item : values()) {
                itemGroup.prepend(item.getItem());
            }
        });
    }

    private static Item registerItem(String name, Function<Item.Properties, Item> itemFactory) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Wuhumc.MOD_ID, name));
        Item item = itemFactory.apply(new Item.Properties().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }
}
