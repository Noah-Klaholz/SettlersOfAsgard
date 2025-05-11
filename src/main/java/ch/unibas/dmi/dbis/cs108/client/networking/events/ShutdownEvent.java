package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

/**
 * Represents an event sent from the server to the client when the game is shutting down.
 * This event is used to notify the client about the reason for the shutdown.
 */
public class ShutdownEvent implements Event {

    /**
     * The reason for the shutdown.
     */
    private final String reason;
    /**
     * The timestamp of the event.
     */
    private final Instant timestamp = Instant.now();

    /**
     * Constructs a ShutdownEvent.
     *
     * @param reason the reason for the shutdown
     */
    public ShutdownEvent(String reason) {
        this.reason = reason;
    }

    /**
     * @return the reason for the shutdown
     */
    public String getReason() {
        return reason;
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
