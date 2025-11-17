package xyz.jinenze.wuhumc.action.impl;

import net.minecraft.network.chat.Component;
import xyz.jinenze.wuhumc.action.Action;
import xyz.jinenze.wuhumc.action.ActionProvider;
import xyz.jinenze.wuhumc.action.Actions;
import xyz.jinenze.wuhumc.action.ServerActionContext;

import java.util.Iterator;
import java.util.function.Function;

public record WSNZActions(int delay, int score, ScoringRule scoringRule, Actions<ServerActionContext> actions) implements ActionProvider<ServerActionContext> {
    @Override
    public Iterator<Action<ServerActionContext>> iterator() {
        return actions.iterator();
    }

    public enum ScoringRule {
        INFINITE(context -> -1, Component.translatable("message.wuhumc.game_wsnz_scoring_rule_infinite")),
        TOP_ONE(context -> 1, Component.translatable("message.wuhumc.game_wsnz_scoring_rule_top_one")),
        TOP_HALF(context -> context.processors().size() / 2, Component.translatable("message.wuhumc.game_wsnz_scoring_rule_top_half"));
        private final Function<ServerActionContext, Integer> getter;
        private final Component message;
        ScoringRule(Function<ServerActionContext, Integer> getter, Component text) {
            this.getter = getter;
            this.message = text;
        }

        public int getTotalPoints(ServerActionContext context) {
            return getter.apply(context);
        }

        public Component getMessage() {
            return message;
        }
    }
}
