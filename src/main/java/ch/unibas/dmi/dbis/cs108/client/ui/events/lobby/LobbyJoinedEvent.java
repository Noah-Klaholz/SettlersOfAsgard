package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.List;

public class LobbyJoinedEvent implements UIEvent {
    private final String lobbyId;
    private final List<String> players;
    private final boolean isHost;

    public LobbyJoinedEvent(String lobbyId, List<String> players, boolean isHost) {
        this.lobbyId = lobbyId;
        this.players = players;
        this.isHost = isHost;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public List<String> getPlayers() {
        return players;
    }

    public boolean isHost() {
        return isHost;
    }

    @Override
    public String getType() {
        return "LOBBYJOINED";
    }
}
