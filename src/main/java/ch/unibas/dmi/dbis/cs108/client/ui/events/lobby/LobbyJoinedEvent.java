package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.List;

/**
 * UIEvent indicating the player successfully joined a lobby.
 */
public class LobbyJoinedEvent implements UIEvent {

    private final String lobbyId;
    private final String lobbyName;
    private final String playerName;
    private final boolean isHost;
    private final List<String> players;

    /**
     * Constructs a LobbyJoinedEvent.
     *
     * @param lobbyId    the ID of the joined lobby
     * @param lobbyName  the name of the joined lobby
     * @param playerName the name of the player
     * @param isHost     true if the player is the host
     * @param players    list of players already in the lobby
     */
    public LobbyJoinedEvent(String lobbyId, String lobbyName, String playerName, boolean isHost, List<String> players) {
        this.lobbyId = lobbyId;
        this.lobbyName = lobbyName;
        this.playerName = playerName;
        this.isHost = isHost;
        this.players = players;
    }

    /**
     * Returns the ID of the joined lobby.
     *
     * @return lobby ID
     */
    public String getLobbyId() {
        return lobbyId;
    }

    /**
     * Returns the name of the joined lobby.
     *
     * @return lobby name
     */
    public String getLobbyName() {
        return lobbyName;
    }

    /**
     * Returns the name of the player.
     *
     * @return player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Returns true if the player is the host.
     *
     * @return true if host, false otherwise
     */
    public boolean isHost() {
        return isHost;
    }

    /**
     * Returns the list of players already in the lobby.
     *
     * @return list of player names
     */
    public List<String> getPlayers() {
        return players;
    }

    /**
     * Returns the type of the event.
     *
     * @return event type
     */
    @Override
    public String getType() {
        return "LOBBY_JOINED";
    }
}
