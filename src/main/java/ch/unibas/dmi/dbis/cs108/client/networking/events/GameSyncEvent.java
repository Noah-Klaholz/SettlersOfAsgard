package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

/**
 * Class representing a game synchronization event.
 * This event is used to synchronize the game state across different clients.
 */
public class GameSyncEvent implements Event {
    /*
     * Timestamp of the event.
     */
    private final Instant timestamp = Instant.now();
    /**
     * The message for the gameStateManager
     */
    private final String message;

    public GameSyncEvent(String message) {
        this.message = message;
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

    /**
     * Getter for the message of the event.
     *
     * @return The message of the event.
     */
    public String getMessage() {
        return message;
    }
}
