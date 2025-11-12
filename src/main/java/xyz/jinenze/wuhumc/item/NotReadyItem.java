package xyz.jinenze.wuhumc.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.init.ModItems;

public class NotReadyItem extends Item {
    public NotReadyItem(Settings settings) {
        super(settings.fireproof().rarity(Rarity.EPIC).maxCount(1));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            var processor = ProcessorManager.get(player);
            var currentGame = ProcessorManager.get(player).getCurrentGame();
            processor.emitListener(currentGame.getNotReadyListener());
            var inventory = player.getInventory();
            inventory.removeStack(inventory.getSelectedSlot());
            inventory.insertStack(inventory.getSelectedSlot(), new ItemStack(ModItems.READY_ITEM));
            player.sendMessage(Text.translatable("message.wuhumc.cancel_ready"), true);
        }
        return ActionResult.PASS;
    }
}
