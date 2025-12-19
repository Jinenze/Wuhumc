package xyz.jinenze.wuhumc.action.impl;

import net.minecraft.network.chat.Component;
import xyz.jinenze.wuhumc.action.ActionProvider;
import xyz.jinenze.wuhumc.action.ServerActionContext;

public record WSNZSubGameData(int totalTime, int scoreFactor, ScoringRule scoringRule,
                              ActionProvider<ServerActionContext> actions) {
    public interface ScoringRule extends ScoreGetter {
        Component getMessage();
    }

    public interface ScoreGetter {
        int getScore(int scorerCount, int scoreFactor, ServerActionContext context);
    }

    public enum ScoringRuleImpl implements ScoringRule {
        NONE((scorerCount, scoreFactor, context) -> 0, Component.literal("")),
        INFINITE((scorerCount, scoreFactor, context) -> scoreFactor, Component.translatable("message.wuhumc.game_wsnz_scoring_rule_infinite")),
        TOP_ONE((scorerCount, scoreFactor, context) -> scorerCount == 1 ? scoreFactor : 0, Component.translatable("message.wuhumc.game_wsnz_scoring_rule_top_one")),
        TOP_HALF((scorerCount, scoreFactor, context) -> scorerCount > context.processors().size() / 2 ? scoreFactor : 0, Component.translatable("message.wuhumc.game_wsnz_scoring_rule_top_half")),
        TOP_THREE((scorerCount, scoreFactor, context) -> scorerCount > 3 ? 0 : scoreFactor, Component.translatable("message.wuhumc.game_wsnz_scoring_rule_top_three")),
        TOP_DECREASE((scorerCount, scoreFactor, context) -> Math.max(scoreFactor - scorerCount, 0), Component.translatable("message.wuhumc.game_wsnz_scoring_rule_top_decrease")),
        ;

        private final ScoreGetter scoreGetter;
        private final Component message;

        ScoringRuleImpl(ScoreGetter scoreGetter, Component message) {
            this.scoreGetter = scoreGetter;
            this.message = message;
        }

        @Override
        public int getScore(int scorerCount, int scoreFactor, ServerActionContext context) {
            return scoreGetter.getScore(scorerCount, scoreFactor, context);
        }

        @Override
        public Component getMessage() {
            return message;
        }
    }
}
