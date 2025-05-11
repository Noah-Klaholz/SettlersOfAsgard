package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;
import java.util.logging.Logger;

/**
 * Represents a notification event in the game.
 * This class encapsulates the details of a notification, including the message.
 */
public class NotificationEvent implements Event {
    /**The timestamp of the event.*/
    private final Instant timestamp = Instant.now();
    /**The message associated with the event.*/
    private final String message;
    /**The artifact ID associated with the event.*/
    private final int artifactId;
    /**The X coordinate associated with the event.*/
    private final int x;
    /**The Y coordinate associated with the event.*/
    private final int y;
    /** Boolean representing if an artifact was found*/
    private final boolean artifactFound;

    /**
     * Constructor for NotificationEvent.
     *
     * @param message The message associated with the notification event.
     */
    public NotificationEvent(String message) {
        Logger.getGlobal().info("NotificationEvent: " + message);
        this.message = message;
        if (message == null || message.isEmpty() || message.equals("NULL")) {
            Logger.getGlobal().warning("NotificationEvent: Null message received" + message);
            this.artifactId = -1;
            this.x = -1;
            this.y = -1;
            this.artifactFound = false;
            return;
        }
        String[] parts = message.split("[$]");
        if (parts.length == 3) {
            this.artifactId = Integer.parseInt(parts[0]);
            this.x = Integer.parseInt(parts[1]);
            this.y = Integer.parseInt(parts[2]);
            this.artifactFound = true;
        } else {
            this.artifactId = -1;
            this.x = -1;
            this.y = -1;
            this.artifactFound = false;
            Logger.getGlobal().warning("NotificationEvent: Invalid message format: " + message);
        }
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

    /**
     * Gets the artifact ID associated with the notification event.
     *
     * @return The artifact ID.
     */
    public int getArtifactId() {
        return artifactId;
    }

    /**
     * Gets the coordinates associated with the notification event.
     *
     * @return The coordinates.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the Y coordinate associated with the notification event.
     *
     * @return The Y coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Checks if an artifact was found.
     *
     * @return True if an artifact was found, false otherwise.
     */
    public boolean isArtifactFound() {
        return artifactFound;
    }
}