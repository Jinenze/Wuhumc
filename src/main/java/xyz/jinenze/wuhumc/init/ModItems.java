package xyz.jinenze.wuhumc.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.item.NotReadyItem;
import xyz.jinenze.wuhumc.item.ReadyItem;

import java.util.function.Function;

public class ModItems {
    public static final Item READY_ITEM = registerItem("ready_item", ReadyItem::new);

    public static final Item NOT_READY_ITEM = registerItem("not_ready_item", NotReadyItem::new);

    public static final ResourceKey<CreativeModeTab> CUSTOM_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Wuhumc.MOD_ID, "item_group"));

    public static final CreativeModeTab CUSTOM_TAB = FabricItemGroup.builder()
            .icon(() -> new ItemStack(READY_ITEM))
            .title(Component.translatable("itemGroup.wuhumc.wuhumc"))
            .build();

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CUSTOM_TAB_KEY, CUSTOM_TAB);
        ItemGroupEvents.modifyEntriesEvent(CUSTOM_TAB_KEY).register(itemGroup -> {
            itemGroup.prepend(READY_ITEM);
            itemGroup.prepend(NOT_READY_ITEM);
        });
    }

    private static Item registerItem(String name, Function<Item.Properties, Item> itemFactory) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Wuhumc.MOD_ID, name));
        Item item = itemFactory.apply(new Item.Properties().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }
}
