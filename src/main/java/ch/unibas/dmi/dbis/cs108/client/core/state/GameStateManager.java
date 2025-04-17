package ch.unibas.dmi.dbis.cs108.client.core.state;

import java.util.logging.Logger;

public class GameStateManager {

    private GameState gameState;
    private static final Logger LOGGER = Logger.getLogger(GameStateManager.class.getName());

    GameStateManager(GameState gameState) {
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public void updateGameState(String message) {
        //TODO: udpate GameState by parsing the message
    }
}
