package ch.unibas.dmi.dbis.cs108.client.core.events;

/**
 * Class representing an event that indicates the start of a game round.
 */
public class GameRoundStartedEvent implements GameEvent {
    private final int roundNumber;

    /**
     * Constructor for GameRoundStartedEvent.
     *
     * @param roundNumber The round number that has started.
     */
    public GameRoundStartedEvent(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    /**
     * Gets the round number that has started.
     *
     * @return The round number.
     */
    public int getRoundNumber() {
        return roundNumber;
    }
}