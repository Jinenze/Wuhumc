package xyz.jinenze.wuhumc.client.init;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.client.gui.ScoreBoard;

public class ModUI {
    private static final Identifier SCORE_BOARD = Identifier.fromNamespaceAndPath(Wuhumc.MOD_ID, "score_board");

    public static void register() {
        HudElementRegistry.addLast(SCORE_BOARD, ScoreBoard::render);
    }
}
