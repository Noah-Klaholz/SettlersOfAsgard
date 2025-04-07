package ch.unibas.dmi.dbis.cs108.client.core.rules;

import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;

import java.util.ArrayList;
import java.util.List;

public class RuleProcessor {
    private List<Rule> rules;

    public RuleProcessor() {
        rules = new ArrayList<>();
    }

    public void addRule(Rule rule) {
        rules.add(rule);
    }

    public void processRules(GameState gameState) {
        for (Rule rule : rules) {
            if (rule.isApplicable(gameState)) {
                rule.apply(gameState);
            }
        }
    }
}