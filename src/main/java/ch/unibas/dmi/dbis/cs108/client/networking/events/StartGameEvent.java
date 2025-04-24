package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

/**
 * Represents an event sent from the server to the client when a game starts.
 * This event is used to notify the client about the start of a game.
 */
public class StartGameEvent implements Event {

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
