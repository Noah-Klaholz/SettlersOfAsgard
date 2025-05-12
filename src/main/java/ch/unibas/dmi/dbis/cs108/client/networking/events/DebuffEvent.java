package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;
import java.util.logging.Logger;

/**
 * Represents a notification event in the game.
 * This class encapsulates the details of a notification, including the message.
 */
public class DebuffEvent implements Event {
    /**The timestamp of the event.*/
    private final Instant timestamp = Instant.now();
    /**The message associated with the event.*/
    private final String message;

    /**
     * Constructor for NotificationEvent.
     *
     * @param message The message associated with the notification event.
     */
    public DebuffEvent(String message) {
        this.message = message;

    }

    /**
     * Gets the timestamp of the event.
     *
     * @return The timestamp.
     */
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the message associated with the notification event.
     *
     * @return The message.
     */
    public String getMessage() {
        return message;
    }
}