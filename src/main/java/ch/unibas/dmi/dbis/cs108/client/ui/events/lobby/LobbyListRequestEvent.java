package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class LobbyListRequestEvent implements UIEvent {
    public LobbyListRequestEvent() {
    }

    @Override
    public String getType() {
        return "LOBBYLISTREQUEST";
    }
}
