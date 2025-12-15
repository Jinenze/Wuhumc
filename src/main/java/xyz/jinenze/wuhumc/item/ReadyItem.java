package xyz.jinenze.wuhumc.item;

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
import xyz.jinenze.wuhumc.action.ServerActionContext;
import xyz.jinenze.wuhumc.action.ServerPlayerProcessor;
import xyz.jinenze.wuhumc.init.ModItems;
import xyz.jinenze.wuhumc.init.ModServerActions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReadyItem extends Item {
    public ReadyItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC));
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        if (player instanceof ServerPlayer serverPlayer) {
            var processor = ProcessorManager.get(serverPlayer);
            var currentGame = processor.getCurrentGame();
            processor.removeListener(currentGame.getNotReadyListener());
            var inventory = serverPlayer.getInventory();
            inventory.removeItemNoUpdate(inventory.getSelectedSlot());
            inventory.add(inventory.getSelectedSlot(), new ItemStack(ModItems.NOT_READY_ITEM.getItem()));
            for (ServerPlayer anotherPlayer : serverPlayer.level().getServer().getPlayerList().getPlayers()) {
                if (ProcessorManager.get(anotherPlayer).event(currentGame.getOnReadyEvent())) {
                    return InteractionResult.PASS;
                }
            }
            List<ServerPlayerProcessor> processors = new ArrayList<>();
            for (ServerPlayer anotherPlayer : serverPlayer.level().getServer().getPlayerList().getPlayers()) {
                if (ProcessorManager.get(anotherPlayer).getCurrentGame().equals(ProcessorManager.get(serverPlayer).getCurrentGame())) {
                    processors.add(ProcessorManager.get(anotherPlayer));
                }
            }
            ProcessorManager.getServerProcessor().emitActions(new ServerActionContext(Collections.unmodifiableList(processors)), ModServerActions.GAME_COUNTDOWN);
        }
        return InteractionResult.PASS;
    }
}
