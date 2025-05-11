package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent indicating that a player has left a lobby.
 */
public class PlayerLeftLobbyEvent implements UIEvent {

    /**
     * The ID of the lobby the player left.
     */
    private final String lobbyId;
    /**
     * The name of the player who left.
     */
    private final String playerName;

    /**
     * Constructs a PlayerLeftLobbyEvent.
     *
     * @param lobbyId    the ID of the lobby the player left
     * @param playerName the name of the player who left
     */
    public PlayerLeftLobbyEvent(String lobbyId, String playerName) {
        this.lobbyId = lobbyId;
        this.playerName = playerName;
    }

    /**
     * Returns the ID of the lobby the player left.
     *
     * @return lobby ID
     */
    public String getLobbyId() {
        return lobbyId;
    }

    /**
     * Returns the name of the player who left.
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
        return "PLAYER_LEFT_LOBBY";
    }
}
