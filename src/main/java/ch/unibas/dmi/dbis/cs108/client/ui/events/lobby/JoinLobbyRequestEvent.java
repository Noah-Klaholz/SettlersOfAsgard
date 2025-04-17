package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class JoinLobbyRequestEvent implements UIEvent {
    private final String lobbyId;
    private final String playerName;

    public JoinLobbyRequestEvent(String lobbyId, String playerName) {
        this.lobbyId = lobbyId;
        this.playerName = playerName;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public String getType() {
        return "JOINLOBBYREQUEST";
    }
}
