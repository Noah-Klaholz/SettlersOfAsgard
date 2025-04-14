package ch.unibas.dmi.dbis.cs108.shared.entities;

import ch.unibas.dmi.dbis.cs108.client.core.rules.RuleProcessor;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.core.events.EventDispatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a game round.
 * This class manages the rounds of the game, including player actions and rule processing.
 */
public class GameRound {
    /**
     * The number of the current round.
     */
    private int number;
    /**
     * The list of player rounds in this game round.
     */
    private List<PlayerRound> playerRounds;

    /**
     * Constructor for the GameRound class.
     * Initializes the round number and the list of player rounds.
     */
    public GameRound() {
        this.number = 0;
        playerRounds = new ArrayList<PlayerRound>();
    }

    /**
     * Adds a player round to the list of player rounds in this game round.
     *
     * @param playerRound The player round to be added.
     */
    public void addPlayerRound(PlayerRound playerRound) {
        playerRounds.add(playerRound);
    }

    //todo: this might need implementation (1min time per round, usages in other classes)
    /**
     * Executes the commands for each player in this round.
     * It processes the commands and applies the rules for the game state.
     *
     * @param gameState   The current game state.
     * @param dispatcher   The event dispatcher for handling events.
     * @param ruleProcessor The rule processor for applying game rules.
     */
    public void executeRound(GameState gameState, EventDispatcher dispatcher, RuleProcessor ruleProcessor) {
        for (PlayerRound pr : playerRounds) {
            pr.executeCommands(gameState, dispatcher);
        }
        // End-of-round logic: apply global rules.
        ruleProcessor.processRules(gameState);
    }

    /**
     * Returns the list of player rounds in this game round.
     *
     * @return The list of player rounds.
     */
    public int getNumber() {
        return number;
    }

    /**
     * Sets the number of this game round.
     *
     * @param number The new round number.
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Increments the round number by 1.
     */
    public void nextRound() {
        this.number++;
    }
}
