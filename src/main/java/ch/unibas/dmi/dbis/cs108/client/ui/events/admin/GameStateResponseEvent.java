package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.Map;

/**
 * UIEvent carrying the overall game state information received from the server.
 */
public class GameStateResponseEvent implements UIEvent {

    private final Map<String, Object> gameStateData;

    /**
     * Constructs a new GameStateResponseEvent.
     *
     * @param gameStateData the game state data
     */
    public GameStateResponseEvent(Map<String, Object> gameStateData) {
        this.gameStateData = gameStateData;
    }

    /**
     * Gets the game state data.
     *
     * @return the game state data
     */
    public Map<String, Object> getGameStateData() {
        return gameStateData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "GAME_STATE_RESPONSE";
    }
}
