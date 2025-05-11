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
    /** Boolean representing if the event is a trap*/
    private final boolean isTrap;
    /** Lost runes int */
    private final int lostRunes;

    /**
     * Constructor for NotificationEvent.
     *
     * @param message The message associated with the notification event.
     */
    public NotificationEvent(String message) {
        this.message = message;

        // Handle empty/null messages
        if (message == null || message.isEmpty() || message.equals("NULL")) {
            this.artifactId = -1;
            this.x = -1;
            this.y = -1;
            this.lostRunes = -1;
            this.artifactFound = false;
            this.isTrap = false;
            return;
        }

        // Handle trap messages (format: "TRAP$lostRunes")
        if (message.startsWith("TRAP")) {
            this.isTrap = true;
            this.artifactFound = false;
            this.artifactId = -1;
            this.x = -1;
            this.y = -1;

            String[] trapParts = message.split("[$]");
            this.lostRunes = (trapParts.length > 1) ? Integer.parseInt(trapParts[1]) : -1;
            return;
        }

        // Handle artifact messages (format: "artifactId$x$y")
        String[] parts = message.split("[$]");
        if (parts.length == 3) {
            // Valid artifact found message
            this.artifactId = Integer.parseInt(parts[0]);
            this.x = Integer.parseInt(parts[1]);
            this.y = Integer.parseInt(parts[2]);
            this.artifactFound = true;
            this.isTrap = false;
            this.lostRunes = -1;
        } else {
            // Invalid message format
            this.artifactId = -1;
            this.x = -1;
            this.y = -1;
            this.artifactFound = false;
            this.isTrap = false;
            this.lostRunes = -1;
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
     * Checks if the event is a trap.
     *
     * @return True if the event is a trap, false otherwise.
     */
    public boolean isTrap() {
        return isTrap;
    }

    /**
     * Gets the lost runes associated with the notification event.
     *
     * @return The lost runes.
     */
    public int getLostRunes() {
        return lostRunes;
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