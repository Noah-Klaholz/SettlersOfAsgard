package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class CreateLobbyRequestEvent implements UIEvent {
    private final String lobbyName;
    private final String hostName;

    public CreateLobbyRequestEvent(String lobbyName, String hostName) {
        this.lobbyName = lobbyName;
        this.hostName = hostName;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public String getHostName() {
        return hostName;
    }

    @Override
    public String getType() {
        return "CREATELOBBYREQUEST";
    }
}
