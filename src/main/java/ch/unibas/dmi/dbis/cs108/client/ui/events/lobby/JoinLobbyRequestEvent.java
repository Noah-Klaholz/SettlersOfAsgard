package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing a request to join a lobby.
 */
public class JoinLobbyRequestEvent implements UIEvent {
    private final String lobbyId;
    private final String playerName;

    /**
     * Constructs a JoinLobbyRequestEvent.
     *
     * @param lobbyId    the ID of the lobby to join
     * @param playerName the name of the player joining
     */
    public JoinLobbyRequestEvent(String lobbyId, String playerName) {
        this.lobbyId = lobbyId;
        this.playerName = playerName;
    }

    /**
     * Returns the ID of the lobby to join.
     *
     * @return lobby ID
     */
    public String getLobbyId() {
        return lobbyId;
    }

    /**
     * Returns the name of the player joining.
     *
     * @return player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Returns the event type identifier.
     *
     * @return event type
     */
    @Override
    public String getType() {
        return "JOIN_LOBBY_REQUEST";
    }
}
