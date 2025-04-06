package ch.unibas.dmi.dbis.cs108.server.core.events;

public class GameRoundStartedEvent implements GameEvent {
    private final int roundNumber;

    public GameRoundStartedEvent(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public int getRoundNumber() {
        return roundNumber;
    }
}