package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent confirming the current player has left the lobby.
 */
public class LobbyLeftEvent implements UIEvent {

    private final String lobbyId;

    /**
     * Constructs a LobbyLeftEvent.
     *
     * @param lobbyId the ID of the lobby left
     */
    public LobbyLeftEvent(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    /**
     * Returns the ID of the lobby left.
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
        return "LOBBY_LEFT";
    }
}
