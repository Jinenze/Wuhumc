package xyz.jinenze.wuhumc.game;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.action.*;
import xyz.jinenze.wuhumc.action.impl.WSNZActions;
import xyz.jinenze.wuhumc.init.ModServerActions;
import xyz.jinenze.wuhumc.init.ModServerEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class WSNZGame extends Game {
    static {
        var itemStack = new ItemStack(Items.DIAMOND);
        itemStack.applyComponents(DataComponentMap.builder().set(DataComponents.CUSTOM_NAME, Component.literal("friendship")).build());
        DIAMOND = itemStack;
    }

    private static final ItemStack DIAMOND;
    private final List<WSNZActions> subActionList = new ArrayList<>(List.of(
            ModServerActions.WSNZ_1_FALL_VOID
            , ModServerActions.WSNZ_2_SHIFT_DOWN
            , ModServerActions.WSNZ_3_DIAMOND
            , ModServerActions.WSNZ_4_CRAFT_AXE
            , ModServerActions.WSNZ_5_ABOVE
            , ModServerActions.WSNZ_6_NOT_ABOVE
    ));
    private Iterator<WSNZActions> iterator;
    private WSNZActions currentGame;
    private int remainPoints;
    private int remainGames;
    private int nextDelay;

    public WSNZGame(ModServerEvents onReadyEvent, Actions<ServerActionContext> gameStartAction, EventListener<ServerPlayer> notReadyListener) {
        super(onReadyEvent, gameStartAction, notReadyListener);
    }

    public boolean hasNext() {
        return iterator.hasNext() && remainGames != 0;
    }

    public WSNZActions next(ServerActionContext context) {
        currentGame = iterator.next();
        remainPoints = currentGame.scoringRule().getTotalPoints(context);
        --remainGames;
        return currentGame;
    }

    public WSNZActions getCurrentGame() {
        return currentGame;
    }

    @Override
    public void addScore(ServerPlayer player) {
        if (remainPoints == 0) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_add_score_failed")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_HAT, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
            return;
        }
        if (currentGame.score() > 0) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_add_score")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        } else {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.wuhumc.game_minus_score")));
            player.connection.send(new ClientboundSoundPacket(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.6f, 1f, 0));
        }
        ProcessorManager.get(player).addScore(currentGame.score());
        --remainPoints;
    }

    @Override
    public void gameStart() {
        super.gameStart();
        this.remainGames = Wuhumc.config.game_settings_wsnz.max_games;
        reroll();
    }

    public void setNextDelay(int nextDelay) {
        this.nextDelay = nextDelay;
    }

    public int getNextDelay() {
        return nextDelay;
    }

    public static ItemStack getDiamond() {
        return DIAMOND.copy();
    }

    public void reroll() {
        Collections.shuffle(subActionList);
        iterator = subActionList.iterator();
    }
}
