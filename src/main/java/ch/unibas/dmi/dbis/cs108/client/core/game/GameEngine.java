package ch.unibas.dmi.dbis.cs108.client.core.game;
/**
import ch.unibas.dmi.dbis.cs108.client.core.events.EventDispatcher;
import ch.unibas.dmi.dbis.cs108.client.core.rules.ResourceGenerationRule;
import ch.unibas.dmi.dbis.cs108.client.core.rules.RuleProcessor;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.core.commands.*;

import java.util.List;


 * The GameEngine class is responsible for managing the game state, processing commands,
 * and handling events. It serves as the main controller for the game logic.

public class GameEngine {
    /**
     * The GameState object representing the current state of the game.

    private GameState gameState;
    /**
     * The EventDispatcher object responsible for handling events in the game.

    private EventDispatcher eventDispatcher;
    /**
     * The RuleProcessor object responsible for processing game rules.

    private RuleProcessor ruleProcessor;

    /**
     * Constructor for the GameEngine class.
     *
     * @param gameState The GameState object representing the current state of the game.

    public GameEngine(GameState gameState) {
        this.gameState = gameState;
        eventDispatcher = new EventDispatcher();
        ruleProcessor = new RuleProcessor();
        // Add default rules.
        ruleProcessor.addRule(new ResourceGenerationRule());
    }

    /**
     * Returns the GameState object representing the current state of the game.
     *
     * @return The GameState object.

    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    /**
     * Processes a turn by executing the given commands and applying game rules.
     *
     * @param commands The list of commands to be executed for the current turn.

    public void processTurn(List<Command> commands) {
        for (Command command : commands) {
            //todo:fix this weird error message
            //command.execute(gameState, eventDispatcher);
        }
        ruleProcessor.processRules(gameState);
        gameState.nextTurn();
    }

    /**
     * Processes a game round by executing the commands for each player and applying rules.
     *
     * @param gameRound The GameRound integer representing the current game round.

    public void processGameRound(int gameRound) {
        //TODO
    }
}*/