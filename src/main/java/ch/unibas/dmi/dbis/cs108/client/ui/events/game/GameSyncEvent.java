package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.Map;

/**
 * Carries a full snapshot of the current game state.
 * Used for initial synchronization when joining a game or reconnecting.
 */
public class GameSyncEvent implements UIEvent {

    private final Map<String, Object> boardState;
    private final Map<String, Map<String, Object>> playersState;
    private final String currentPlayerTurn;
    private final int currentTurnNumber;

    /**
     * Constructs a GameSyncEvent.
     *
     * @param boardState        the state of the board
     * @param playersState      the state of all players
     * @param currentPlayerTurn the player whose turn it is
     * @param currentTurnNumber the current turn number
     */
    public GameSyncEvent(Map<String, Object> boardState, Map<String, Map<String, Object>> playersState,
            String currentPlayerTurn, int currentTurnNumber) {
        this.boardState = boardState;
        this.playersState = playersState;
        this.currentPlayerTurn = currentPlayerTurn;
        this.currentTurnNumber = currentTurnNumber;
    }

    /**
     * @return the board state
     */
    public Map<String, Object> getBoardState() {
        return boardState;
    }

    /**
     * @return the players' state
     */
    public Map<String, Map<String, Object>> getPlayersState() {
        return playersState;
    }

    /**
     * @return the current player's turn
     */
    public String getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    /**
     * @return the current turn number
     */
    public int getCurrentTurnNumber() {
        return currentTurnNumber;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "GAME_SYNC";
    }
}
