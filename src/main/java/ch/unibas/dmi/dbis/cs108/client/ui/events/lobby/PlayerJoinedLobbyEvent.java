package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent indicating that a player has joined a lobby.
 */
public class PlayerJoinedLobbyEvent implements UIEvent {

    /**
     * The ID of the lobby the player joined.
     */
    private final String lobbyId;
    /**
     * The name of the player who joined.
     */
    private final String playerName;

    /**
     * Constructs a PlayerJoinedLobbyEvent.
     *
     * @param lobbyId    the ID of the lobby the player joined
     * @param playerName the name of the player who joined
     */
    public PlayerJoinedLobbyEvent(String lobbyId, String playerName) {
        this.lobbyId = lobbyId;
        this.playerName = playerName;
    }

    /**
     * Returns the ID of the lobby the player joined.
     *
     * @return lobby ID
     */
    public String getLobbyId() {
        return lobbyId;
    }

    /**
     * Returns the name of the player who joined.
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
        return "PLAYER_JOINED_LOBBY";
    }
}
