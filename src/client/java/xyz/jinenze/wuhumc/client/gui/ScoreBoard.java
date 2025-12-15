package xyz.jinenze.wuhumc.client.gui;

import com.ibm.icu.impl.Pair;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import xyz.jinenze.wuhumc.client.WuhumcClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ScoreBoard {
    private static final List<ScoreBoardElement> elements = new ArrayList<>();

    public static void render(GuiGraphics context, DeltaTracker tickCounter) {
        elements.removeIf(element -> element.render.apply(context));
    }

    private static class ScoreBoardElement {
        private final float startTime;
        private final Function<GuiGraphics, Boolean> render;

        private ScoreBoardElement(String player, int currentScore, int previousScore, float offset, float y) {
            startTime = Util.getMillis() + offset;
            render = context -> {
                float currentTotalTime = Util.getMillis() - startTime;
                if (currentTotalTime < 0) {
                    return false;
                }
                if (currentTotalTime > WuhumcClient.config.score_board.total_visible_time) {
                    return true;
                }
                float displayPortion;
                float halfTime = WuhumcClient.config.score_board.half_visible_time;
                float totalTime = WuhumcClient.config.score_board.total_visible_time;

                if (currentTotalTime > halfTime) {
                    float progress = (currentTotalTime - halfTime) / (totalTime - halfTime);
                    displayPortion = 1 - (float) WuhumcClient.config.score_board.out_easing_method.apply(progress);
                } else {
                    float progress = currentTotalTime / halfTime;
                    displayPortion = (float) WuhumcClient.config.score_board.in_easing_method.apply(progress);
                }
                float x = context.guiWidth() - WuhumcClient.config.score_board.width * displayPortion;
                context.pose().pushMatrix().translate(x, y);
                context.drawString(Minecraft.getInstance().font, player, 0, 0, ARGB.color(255, 255, 255));
                int difference = currentScore - previousScore;
                context.drawString(Minecraft.getInstance().font, String.valueOf(currentScore), WuhumcClient.config.score_board.cap_1, 0, ARGB.color(255, 255, 255));
                if (difference > 0) {
                    context.drawString(Minecraft.getInstance().font, "+" + difference, WuhumcClient.config.score_board.cap_2, 0, ARGB.color(0, 255, 0));
                } else if (difference == 0) {
                    context.drawString(Minecraft.getInstance().font, "0", WuhumcClient.config.score_board.cap_2, 0, ARGB.color(255, 255, 255));
                } else {
                    context.drawString(Minecraft.getInstance().font, String.valueOf(difference), WuhumcClient.config.score_board.cap_2, 0, ARGB.color(255, 0, 0));
                }
                context.pose().popMatrix();
                return false;
            };
        }
    }

    public static void processMap(Map<String, Pair<Integer, Integer>> map) {
        float offset = 0;
        float y = 0;
        var linkMap = map.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().first.compareTo(e1.getValue().first))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        for (var element : linkMap.entrySet()) {
            elements.add(new ScoreBoardElement(element.getKey(), element.getValue().first, element.getValue().second, offset, y));
            offset += WuhumcClient.config.score_board.element_time_offset;
            y += WuhumcClient.config.score_board.element_cap;
        }
    }
}
