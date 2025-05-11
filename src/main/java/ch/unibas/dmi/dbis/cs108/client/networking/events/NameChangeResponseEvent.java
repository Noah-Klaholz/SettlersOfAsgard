package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

/**
 * Represents a response event for a name change request in the game.
 * This class encapsulates the details of the response, including success status, new name, and message.
 */
public class NameChangeResponseEvent implements Event {
    /**
     * The timestamp of when the event occurred.
     */
    private final boolean success;
    /**
     * The new name assigned to the player.
     */
    private final String newName;
    /**
     * The message associated with the response.
     */
    private final String message;

    /**
     * Constructor for NameChangeResponseEvent.
     *
     * @param success Indicates whether the name change was successful.
     * @param newName The new name assigned to the player.
     * @param message A message associated with the response.
     */
    public NameChangeResponseEvent(boolean success, String newName, String message) {
        this.success = success;
        this.newName = newName;
        this.message = message;
    }

    /**
     * Getter for the success status of the name change.
     *
     * @return true if the name change was successful, false otherwise.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Getter for the new name assigned to the player.
     *
     * @return The new name.
     */
    public String getNewName() {
        return newName;
    }

    /**
     * Getter for the message associated with the response.
     *
     * @return The message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Getter for the timestamp of the event.
     *
     * @return The timestamp of the event.
     */
    @Override
    public Instant getTimestamp() {
        return null;
    }
}