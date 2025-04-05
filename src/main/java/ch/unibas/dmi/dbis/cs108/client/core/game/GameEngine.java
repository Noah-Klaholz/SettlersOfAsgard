package ch.unibas.dmi.dbis.cs108.client.core.game;

import ch.unibas.dmi.dbis.cs108.client.core.entities.GameRound;
import ch.unibas.dmi.dbis.cs108.client.core.events.EventDispatcher;
import ch.unibas.dmi.dbis.cs108.client.core.rules.ResourceGenerationRule;
import ch.unibas.dmi.dbis.cs108.client.core.rules.RuleProcessor;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.core.commands.*;

import java.util.List;

public class GameEngine {
    private GameState gameState;
    private EventDispatcher eventDispatcher;
    private RuleProcessor ruleProcessor;

    public GameEngine(GameState gameState) {
        this.gameState = gameState;
        eventDispatcher = new EventDispatcher();
        ruleProcessor = new RuleProcessor();
        // Add default rules.
        ruleProcessor.addRule(new ResourceGenerationRule());
    }

    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public void processTurn(List<Command> commands) {
        for (Command command : commands) {
            //todo:fix this weird error message
            //command.execute(gameState, eventDispatcher);
        }
        ruleProcessor.processRules(gameState);
        gameState.nextTurn();
    }

    public void processGameRound(GameRound gameRound) {
        gameRound.executeRound(gameState, eventDispatcher, ruleProcessor);
    }
}