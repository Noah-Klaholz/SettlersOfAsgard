package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.core.state.GameStateManager;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.time.Instant;
import java.util.logging.Logger;

public class GameSyncEvent implements UIEvent {
    /**
     * The game state manager.
     */
    private final GameStateManager gameStateManager;

    public GameSyncEvent(String args, GameStateManager gameStateManager) {
        Logger.getGlobal().info(args);
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
