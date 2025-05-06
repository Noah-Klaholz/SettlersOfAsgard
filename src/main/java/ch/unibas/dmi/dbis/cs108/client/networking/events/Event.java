package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

/**
 * Represents an event in the game.
 * This interface defines the structure for events that can occur in the game.
 */
public interface Event {
    /**
     * Getter for the timestamp of the event.
     *
     * @return The timestamp of the event.
     */
    Instant getTimestamp();
}