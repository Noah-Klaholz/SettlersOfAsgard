package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class LeaveLobbyRequestEvent implements UIEvent {
    private final String lobbyId;

    public LeaveLobbyRequestEvent(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    @Override
    public String getType() {
        return "LEAVELOBBYREQUEST";
    }
}