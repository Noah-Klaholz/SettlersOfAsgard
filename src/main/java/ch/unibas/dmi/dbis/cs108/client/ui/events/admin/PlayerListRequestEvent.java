package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent requesting a list of players, either all online or in a specific
 * lobby.
 */
public class PlayerListRequestEvent implements UIEvent {

    /**
     * The ID of the lobby for which players are requested. If null, all online
     * players are requested.
     */
    private final String lobbyId;

    /**
     * Constructs a request for all online players.
     */
    public PlayerListRequestEvent() {
        this.lobbyId = null;
    }

    /**
     * Constructs a request for players in a specific lobby.
     *
     * @param lobbyId the ID of the lobby
     */
    public PlayerListRequestEvent(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    /**
     * Gets the lobby ID for which players are requested.
     *
     * @return the lobby ID, or null if requesting all players
     */
    public String getLobbyId() {
        return lobbyId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "PLAYER_LIST_REQUEST";
    }
}
