package ch.unibas.dmi.dbis.cs108.client.core.events;

public class GameRoundEndedEvent implements GameEvent {
    private final int roundNumber;

    public GameRoundEndedEvent(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public int getRoundNumber() {
        return roundNumber;
    }
}