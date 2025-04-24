package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

/**
 * Represents an event sent from the server to the client when a player's turn ends.
 * This event is used to notify the client about the end of a turn and potentially
 * the start of the next player's turn.
 */
public class EndTurnEvent implements Event {
    /**
     * The timestamp of the event.
     */
    private final Instant timestamp = Instant.now();
    /**
     * The name of the player whose turn it is now.
     */
    private final String nextPlayerName;

    /**
     * Constructs an EndTurnEvent.
     *
     * @param nextPlayerName the name of the player whose turn it is now
     */
    public EndTurnEvent(String nextPlayerName) {
        this.nextPlayerName = nextPlayerName;
    }

    /**
     * @return the name of the next player
     */
    public String getNextPlayerName() {
        return nextPlayerName;
    }

    /**
     * Getter for the timestamp of the event.
     *
     * @return The timestamp of the event.
     */
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }
}
