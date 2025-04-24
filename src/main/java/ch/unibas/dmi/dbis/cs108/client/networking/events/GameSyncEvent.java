package ch.unibas.dmi.dbis.cs108.client.networking.events;

import ch.unibas.dmi.dbis.cs108.client.core.state.GameStateManager;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;

import java.time.Instant;

public class GameSyncEvent implements Event{
    /**
     * The game state manager.
     */
    private final GameStateManager gameStateManager;

    /**
     * Timestamp of the event.
     */
    private final Instant timestamp = Instant.now();

    public GameSyncEvent(String args, GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
        gameStateManager.updateGameState(args);
    }

    /**
     * Getter for the gameState.
     *
     * @return The game state.
     */
    public GameState getGameState() {
        return gameStateManager.getGameState();
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
