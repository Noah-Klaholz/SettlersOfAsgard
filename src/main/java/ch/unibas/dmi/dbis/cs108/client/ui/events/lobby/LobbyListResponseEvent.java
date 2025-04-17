package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.controllers.LobbyScreenController;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.List;

public class LobbyListResponseEvent implements UIEvent {
    private final List<LobbyScreenController.GameLobby> lobbies;

    public LobbyListResponseEvent(List<LobbyScreenController.GameLobby> lobbies) {
        this.lobbies = lobbies;
    }

    public List<LobbyScreenController.GameLobby> getLobbies() {
        return lobbies;
    }

    @Override
    public String getType() {
        return "LOBBYLISTRESPONSE";
    }
}
