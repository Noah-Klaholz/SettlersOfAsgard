package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameStateManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.logging.Logger;


/**
 * Represents a game synchronization event that updates the game state.
 * This event is used to synchronize the game state between the server and the client.
 */
public class GameSyncEvent implements UIEvent {
    /**
     * The game state manager.
     */
    private final GameStateManager gameStateManager;

    /**
     * Constructs a new GameSyncEvent.
     *
     * @param args              The arguments for the event.
     * @param gameStateManager  The game state manager.
     */
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
     * Returns the event type string.
     *
     * @return event type string
     */
    @Override
    public String getType() {
        return "SyncEvent";
    }
}
