package ch.unibas.dmi.dbis.cs108.client.networking.events;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

public class LobbyJoinedEvent implements Event {
    private final String lobbyId;
    private final List<String> players; // the players in the lobby
    private final String player; // the player that just joined the lobby
    private final boolean isHost;
    private final Instant timestamp = Instant.now();

    public LobbyJoinedEvent(String lobbyId, String players, boolean isHost) {
        Logger.getGlobal().info("LobbyJoinedEvent constructor called");
        this.lobbyId = lobbyId;
        if (players.contains("%")) {
            Logger.getGlobal().info("Players contains %");
            this.players = List.of(players.split("%"));
            this.player = this.players.get(this.players.size() - 1);
        } else { // if only 1 player is in the lobby, then only the playerName gets transmitted
            this.players = List.of(players);
            this.player = players;
        }
        this.isHost = isHost;
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

    public String getPlayer() {
        return player;
    }

    public boolean isHost() {
        return isHost;
    }

    public String getType() {
        return "LOBBYJOINED";
    }
}
