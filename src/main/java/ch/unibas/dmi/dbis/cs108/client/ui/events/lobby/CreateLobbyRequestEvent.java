package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing a request to create a new lobby.
 */
public class CreateLobbyRequestEvent implements UIEvent {
    /**
     * The name of the lobby to create.
     */
    private final String lobbyName;
    /**
     * The name of the host (creator).
     */
    private final String hostName;
    /**
     * The maximum number of players allowed in the lobby.
     */
    private final int maxPlayers;

    /**
     * Constructs a CreateLobbyRequestEvent.
     *
     * @param lobbyName  the name of the lobby to create
     * @param hostName   the name of the host (creator)
     * @param maxPlayers the maximum number of players allowed in the lobby
     */
    public CreateLobbyRequestEvent(String lobbyName, String hostName, int maxPlayers) {
        this.lobbyName = lobbyName;
        this.hostName = hostName;
        this.maxPlayers = maxPlayers;
    }

    /**
     * Returns the name of the lobby to create.
     *
     * @return lobby name
     */
    public String getLobbyName() {
        return lobbyName;
    }

    /**
     * Returns the name of the host (creator).
     *
     * @return host name
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Returns the maximum number of players allowed in the lobby.
     *
     * @return maximum number of players
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Returns the event type identifier.
     *
     * @return event type
     */
    @Override
    public String getType() {
        return "CREATE_LOBBY_REQUEST";
    }
}
