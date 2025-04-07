package ch.unibas.dmi.dbis.cs108.client.core.entities;

import ch.unibas.dmi.dbis.cs108.client.core.rules.RuleProcessor;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.core.events.EventDispatcher;

import java.util.ArrayList;
import java.util.List;

public class GameRound {
    private int number;
    private List<PlayerRound> playerRounds;

    public GameRound() {
        this.number = 0;
        playerRounds = new ArrayList<PlayerRound>();
    }

    public void addPlayerRound(PlayerRound playerRound) {
        playerRounds.add(playerRound);
    }

    //todo: this might need implementation (1min time per round, usages in other classes)
    public void executeRound(GameState gameState, EventDispatcher dispatcher, RuleProcessor ruleProcessor) {
        for (PlayerRound pr : playerRounds) {
            pr.executeCommands(gameState, dispatcher);
        }
        // End-of-round logic: apply global rules.
        ruleProcessor.processRules(gameState);
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void nextRound() {
        this.number++;
    }
}
