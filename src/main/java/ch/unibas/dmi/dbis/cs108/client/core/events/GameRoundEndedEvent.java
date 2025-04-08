package ch.unibas.dmi.dbis.cs108.client.core.events;

/**
 * Class representing an event that indicates the end of a game round.
 */
public class GameRoundEndedEvent implements GameEvent {
    /**
     * The round number that has ended.
     */
    private final int roundNumber;

    /**
     * Constructor for GameRoundEndedEvent.
     *
     * @param roundNumber The round number that has ended.
     */
    public GameRoundEndedEvent(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    /**
     * Gets the round number that has ended.
     *
     * @return The round number.
     */
    public int getRoundNumber() {
        return roundNumber;
    }
}