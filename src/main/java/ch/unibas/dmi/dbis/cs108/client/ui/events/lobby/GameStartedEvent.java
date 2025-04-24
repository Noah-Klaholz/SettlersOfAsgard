package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent indicating the game has started for the lobby the player was in.
 */
public class GameStartedEvent implements UIEvent {

    private final String lobbyId;

    /**
     * Constructs a GameStartedEvent.
     *
     * @param lobbyId the ID of the lobby where the game started
     */
    public GameStartedEvent(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    /**
     * Returns the ID of the lobby where the game started.
     *
     * @return lobby ID
     */
    public String getLobbyId() {
        return lobbyId;
    }

    /**
     * Returns the event type identifier.
     *
     * @return the event type
     */
    @Override
    public String getType() {
        return "GAME_STARTED";
    }
}
