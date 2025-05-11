package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

/**
 * Represents a notification event in the game.
 * This class encapsulates the details of a notification, including the message.
 */
public class NotificationEvent implements Event {
    /**
     * The timestamp of when the event occurred.
     */
    private final Instant timestamp = Instant.now();
    /**
     * The message associated with the notification event.
     */
    private final String message;

    /**
     * Constructor for NotificationEvent.
     *
     * @param message The message associated with the notification event.
     */
    public NotificationEvent(String message) {
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