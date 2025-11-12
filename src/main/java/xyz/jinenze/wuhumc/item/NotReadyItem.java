package xyz.jinenze.wuhumc.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.init.ModItems;

public class NotReadyItem extends Item {
    public NotReadyItem(Properties properties) {
        super(properties.fireResistant().rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        if (player instanceof ServerPlayer serverPlayer) {
            var processor = ProcessorManager.get(serverPlayer);
            var currentGame = ProcessorManager.get(serverPlayer).getCurrentGame();
            processor.emitListener(currentGame.getNotReadyListener());
            var inventory = serverPlayer.getInventory();
            inventory.removeItemNoUpdate(inventory.getSelectedSlot());
            inventory.add(inventory.getSelectedSlot(), new ItemStack(ModItems.READY_ITEM));
            serverPlayer.sendSystemMessage(Component.translatable("message.wuhumc.cancel_ready"), true);
        }
        return InteractionResult.PASS;
    }
}
