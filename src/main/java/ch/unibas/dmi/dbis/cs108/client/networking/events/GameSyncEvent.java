package ch.unibas.dmi.dbis.cs108.client.networking.events;

import ch.unibas.dmi.dbis.cs108.client.core.state.GameStateManager;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;

import java.time.Instant;

public class GameSyncEvent implements Event{
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

    public String getMessage() {
        return message;
    }
}
