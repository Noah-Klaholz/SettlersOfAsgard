package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

/**
 * Represents an event that occurs when a player's name changes in the game.
 * This class encapsulates the details of the name change event, including the new name.
 */
public class NameChangedEvent implements Event {
    /**
     * The timestamp of when the event occurred.
     */
    private final Instant timestamp = Instant.now();
    /**
     * The new name of the player.
     */
    private final String newName;

    /**
     * Constructor for NameChangedEvent.
     *
     * @param newName The new name of the player.
     */
    public NameChangedEvent(String newName) {
        this.newName = newName;
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
     * Getter for the new name of the player.
     *
     * @return The new name of the player.
     */
    public String getNewName() {
        return newName;
    }
}