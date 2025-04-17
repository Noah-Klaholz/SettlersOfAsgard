package ch.unibas.dmi.dbis.cs108.client.core.state;

import java.util.logging.Logger;

/**
 * This class is responsible for managing a gameState.
 * It will update the gameState based on a message from the server.
 */
public class GameStateManager {

    /** The gameState object managed by this class */
    private GameState gameState;
    /** Logger to log logging*/
    private static final Logger LOGGER = Logger.getLogger(GameStateManager.class.getName());

    /**
     * Creates a new object of this class
     *
     * @param gameState the gameState to manage
     */
    GameStateManager(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Gets the current gameState
     * @return the gameState
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Sets the current gameState
     *
     * @param gameState the gameState to set
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Updates the gameState by parsing the message from the server.
     *
     * @param message the message from the server.
     * @see ch.unibas.dmi.dbis.cs108.server.core.model.GameStateSerializer
     */
    public void updateGameState(String message) {
        //TODO: update GameState by parsing the message
    }
}
