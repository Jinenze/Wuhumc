package xyz.jinenze.wuhumc.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.item.NotReadyItem;
import xyz.jinenze.wuhumc.item.ReadyItem;

import java.util.function.Function;

public class ModItems {
    public static final Item READY_ITEM = registerItem("ready_item", ReadyItem::new);

    public static final Item NOT_READY_ITEM = registerItem("not_ready_item", NotReadyItem::new);

    public static final RegistryKey<ItemGroup> CUSTOM_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(Wuhumc.MOD_ID, "item_group"));

    public static final ItemGroup CUSTOM_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(READY_ITEM))
            .displayName(Text.translatable("itemGroup.wuhumc.wuhumc"))
            .build();

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, CUSTOM_ITEM_GROUP_KEY, CUSTOM_ITEM_GROUP);
        ItemGroupEvents.modifyEntriesEvent(CUSTOM_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(READY_ITEM);
            itemGroup.add(NOT_READY_ITEM);
        });
    }

    private static Item registerItem(String name, Function<Item.Settings, Item> itemFactory) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Wuhumc.MOD_ID, name));

        Item item = itemFactory.apply(new Item.Settings().registryKey(itemKey));

        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }
}
