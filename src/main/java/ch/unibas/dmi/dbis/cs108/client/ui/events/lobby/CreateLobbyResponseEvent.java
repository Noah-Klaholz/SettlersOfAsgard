package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing the response to a create lobby request.
 * Indicates the lobby was created and joined by the creator.
 */
public class CreateLobbyResponseEvent implements UIEvent {

    private final String lobbyName;
    private final String hostName;

    /**
     * Constructs a CreateLobbyResponseEvent.
     *
     * @param lobbyName the name of the created lobby
     * @param hostName  the name of the host (creator)
     */
    public CreateLobbyResponseEvent(String lobbyName, String hostName) {
        this.lobbyName = lobbyName;
        this.hostName = hostName;
    }

    /**
     * Returns the name of the created lobby.
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

    @Override
    public String getType() {
        return "CREATE_LOBBY_RESPONSE";
    }
}
