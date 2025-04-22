package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing a request to leave a lobby.
 */
public class LeaveLobbyRequestEvent implements UIEvent {
    private final String lobbyId;

    /**
     * Constructs a LeaveLobbyRequestEvent.
     *
     * @param lobbyId the ID of the lobby to leave
     */
    public LeaveLobbyRequestEvent(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    /**
     * Returns the ID of the lobby to leave.
     *
     * @return lobby ID
     */
    public String getLobbyId() {
        return lobbyId;
    }

    @Override
    public String getType() {
        return "LEAVE_LOBBY_REQUEST";
    }
}
