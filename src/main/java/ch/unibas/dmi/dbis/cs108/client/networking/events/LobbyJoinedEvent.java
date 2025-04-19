package ch.unibas.dmi.dbis.cs108.client.networking.events;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.time.Instant;
import java.util.List;

public class LobbyJoinedEvent implements Event {
    private final String lobbyId;
    private final List<String> players;
    private final String player;
    private final boolean isHost;
    private final Instant timestamp = Instant.now();

    public LobbyJoinedEvent(String lobbyId, String players, boolean isHost) {
        this.lobbyId = lobbyId;
        this.players = List.of(players.split("%"));
        this.isHost = isHost;
        this.player = this.players.get(players.length() - 1);
    }

    public LobbyJoinedEvent(String lobbyId, String player) {
        this.lobbyId = lobbyId;
        this.player = player;
        this.isHost = false;
        this.players = null;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
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

    public String getType() {
        return "LOBBYJOINED";
    }
}
