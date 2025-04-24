package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.controllers.LobbyScreenController.GameLobby;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.List;

/**
 * UIEvent carrying the list of available lobbies from the server.
 */
public class LobbyListResponseEvent implements UIEvent {

    private final List<GameLobby> lobbies;

    /**
     * Constructs a LobbyListResponseEvent.
     *
     * @param lobbies the list of available lobbies
     */
    public LobbyListResponseEvent(List<GameLobby> lobbies) {
        this.lobbies = lobbies;
    }

    /**
     * Returns the list of available lobbies.
     *
     * @return list of lobbies
     */
    public List<GameLobby> getLobbies() {
        return lobbies;
    }

    @Override
    public String getType() {
        return "LOBBY_LIST_RESPONSE";
    }
}
