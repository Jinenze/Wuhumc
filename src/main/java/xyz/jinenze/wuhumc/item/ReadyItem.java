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

public class ReadyItem extends Item {
    public ReadyItem(Settings settings) {
        super(settings.fireproof().rarity(Rarity.EPIC).maxCount(1));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            if (hand.equals(Hand.OFF_HAND)) {
                var processor = ProcessorManager.getInstance().getProcessor(player);
                var currentGame = ProcessorManager.getInstance().getProcessor(player).getCurrentGame();
                processor.removeListener(currentGame.notReadyListener());
                processor.listen(currentGame.gameStartListener());
                var inventory = player.getInventory();
                inventory.removeStack(40);
                inventory.insertStack(40, new ItemStack(ModItems.NOT_READY_ITEM));
                for (ServerPlayerEntity player1 : player.getEntityWorld().getServer().getPlayerManager().getPlayerList()) {
                    if (ProcessorManager.getInstance().getProcessor(player1).emitEvent(currentGame.onReadyEvent())) {
                        return ActionResult.PASS;
                    }
                }
                for (ServerPlayerEntity player1 : player.getEntityWorld().getServer().getPlayerManager().getPlayerList()) {
                    ProcessorManager.getInstance().getProcessor(player1).emitEvent(currentGame.gameStartEvent());
                }
            } else {
                player.sendMessage(Text.translatable("message.wuhumc.ready_alert"), true);
            }
        }
        return ActionResult.PASS;
    }
}
