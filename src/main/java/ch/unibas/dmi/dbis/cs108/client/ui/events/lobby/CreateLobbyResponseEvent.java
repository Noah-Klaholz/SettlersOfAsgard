package ch.unibas.dmi.dbis.cs108.client.ui.events.lobby;

import ch.unibas.dmi.dbis.cs108.client.networking.events.LobbyEvent;

import java.time.Instant;

public class CreateLobbyResponseEvent {
    private final Instant timestamp = Instant.now();
    private final String playerName;
    private final String lobbyName;

    public CreateLobbyResponseEvent( String playerName, String lobbyName) {
        this.playerName = playerName;
        this.lobbyName = lobbyName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getLobbyName() {
        return lobbyName;
    }

}
