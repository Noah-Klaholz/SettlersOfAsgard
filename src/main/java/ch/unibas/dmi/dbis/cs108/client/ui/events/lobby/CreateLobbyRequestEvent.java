package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing a request to create a new lobby.
 */
public class CreateLobbyRequestEvent implements UIEvent {
    private final String lobbyName;
    private final String hostName;

    /**
     * Constructs a CreateLobbyRequestEvent.
     *
     * @param lobbyName the name of the lobby to create
     * @param hostName  the name of the host (creator)
     */
    public CreateLobbyRequestEvent(String lobbyName, String hostName) {
        this.lobbyName = lobbyName;
        this.hostName = hostName;
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
     * Returns the event type identifier.
     *
     * @return event type
     */
    @Override
    public String getType() {
        return "CREATE_LOBBY_REQUEST";
    }
}
