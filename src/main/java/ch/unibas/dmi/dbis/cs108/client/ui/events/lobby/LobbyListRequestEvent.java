package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing a request for the list of available lobbies.
 */
public class LobbyListRequestEvent implements UIEvent {

    /**
     * Constructs a LobbyListRequestEvent.
     */
    public LobbyListRequestEvent() {
    }

    @Override
    public String getType() {
        return "LOBBY_LIST_REQUEST";
    }
}
