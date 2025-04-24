package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;

/**
 * Carries a full snapshot of the current game state.
 * Used for initial synchronization when joining a game or reconnecting.
 */
public class GameSyncEvent implements UIEvent {

    private final GameState gameState;

    /**
     * Constructs a GameSyncEvent.
     *
     * @param gameState the game state containing the board state, players' state,
     */
    public GameSyncEvent(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * @return the game state
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "GAME_SYNC";
    }
}
