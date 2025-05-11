package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent indicating an update to a lobby's details.
 */
public class LobbyUpdateEvent implements UIEvent {

    /**
     * The ID of the lobby.
     */
    private final String lobbyId;
    /**
     * The new status of the lobby.
     */
    private final String newStatus;
    /**
     * The current number of players in the lobby.
     */
    private final int currentPlayers;
    /**
     * The maximum number of players allowed in the lobby.
     */
    private final int maxPlayers;
    /**
     * The new host name of the lobby.
     */
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

    /**
     * Returns the type of this event.
     *
     * @return the type of this event
     */
    @Override
    public String getType() {
        return "LOBBY_UPDATE";
    }
}
