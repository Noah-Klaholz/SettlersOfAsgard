package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent sent by the host to request the server to start the game for the
 * lobby.
 */
public class StartGameRequestEvent implements UIEvent {

    private final String lobbyId;

    /**
     * Constructs a StartGameRequestEvent.
     *
     * @param lobbyId the ID of the lobby to start the game for
     */
    public StartGameRequestEvent(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    /**
     * Returns the ID of the lobby to start the game for.
     *
     * @return lobby ID
     */
    public String getLobbyId() {
        return lobbyId;
    }

    /**
     * Returns the event type identifier.
     *
     * @return event type
     */
    @Override
    public String getType() {
        return "START_GAME_REQUEST";
    }
}
