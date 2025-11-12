package xyz.jinenze.wuhumc.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import xyz.jinenze.wuhumc.action.PlayerProcessor;
import xyz.jinenze.wuhumc.action.ProcessorManager;
import xyz.jinenze.wuhumc.action.ServerActionContext;
import xyz.jinenze.wuhumc.init.ModItems;
import xyz.jinenze.wuhumc.init.ModServerActions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReadyItem extends Item {
    public ReadyItem(Settings settings) {
        super(settings.fireproof().rarity(Rarity.EPIC).maxCount(1));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            var processor = ProcessorManager.get(player);
            var currentGame = processor.getCurrentGame();
            processor.removeListener(currentGame.getNotReadyListener());
            var inventory = player.getInventory();
            inventory.removeStack(inventory.getSelectedSlot());
            inventory.insertStack(inventory.getSelectedSlot(), new ItemStack(ModItems.NOT_READY_ITEM));
            for (ServerPlayerEntity anotherPlayer : player.getEntityWorld().getServer().getPlayerManager().getPlayerList()) {
                if (ProcessorManager.get(anotherPlayer).event(currentGame.getOnReadyEvent())) {
                    return ActionResult.PASS;
                }
            }
            List<PlayerProcessor<ServerPlayerEntity>> processors = new ArrayList<>();
            for (ServerPlayerEntity anotherPlayer : player.getEntityWorld().getServer().getPlayerManager().getPlayerList()) {
                if (ProcessorManager.get(anotherPlayer).getCurrentGame().equals(ProcessorManager.get(player).getCurrentGame())) {
                    processors.add(ProcessorManager.get(anotherPlayer));
                }
            }
            ProcessorManager.getServerProcessor().emitActions(new ServerActionContext(Collections.unmodifiableList(processors)), ModServerActions.GAME_COUNTDOWN);
        }
        return ActionResult.PASS;
    }
}
