package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent indicating an update to a lobby's details.
 */
public class LobbyUpdateEvent implements UIEvent {

    private final String lobbyId;
    private final String newStatus;
    private final int currentPlayers;
    private final int maxPlayers;
    private final String newHostName;

    /**
     * Constructs a LobbyUpdateEvent.
     *
     * @param lobbyId        the ID of the lobby
     * @param newStatus      the new status, or null if not changed
     * @param currentPlayers the new player count, or -1 if not changed
     * @param maxPlayers     the new max player count, or -1 if not changed
     * @param newHostName    the new host name, or null if not changed
     */
    public LobbyUpdateEvent(String lobbyId, String newStatus, int currentPlayers, int maxPlayers, String newHostName) {
        this.lobbyId = lobbyId;
        this.newStatus = newStatus;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
        this.newHostName = newHostName;
    }

    /**
     * Returns the ID of the lobby.
     *
     * @return lobby ID
     */
    public String getLobbyId() {
        return lobbyId;
    }

    /**
     * Returns the new status, or null if not changed.
     *
     * @return new status
     */
    public String getNewStatus() {
        return newStatus;
    }

    /**
     * Returns the new player count, or -1 if not changed.
     *
     * @return current player count
     */
    public int getCurrentPlayers() {
        return currentPlayers;
    }

    /**
     * Returns the new max player count, or -1 if not changed.
     *
     * @return max player count
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Returns the new host name, or null if not changed.
     *
     * @return new host name
     */
    public String getNewHostName() {
        return newHostName;
    }

    @Override
    public String getType() {
        return "LOBBY_UPDATE";
    }
}
