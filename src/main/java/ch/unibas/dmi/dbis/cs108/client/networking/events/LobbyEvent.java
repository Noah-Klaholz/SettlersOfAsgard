package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

public class LobbyEvent implements Event {
    private final Instant timestamp = Instant.now();
    private final LobbyAction action;
    private final String playerName;
    private final String lobbyName;

    public LobbyEvent(LobbyAction action, String playerName, String lobbyName) {
        this.action = action;
        this.playerName = playerName;
        this.lobbyName = lobbyName;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public LobbyAction getAction() {
        return action;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public enum LobbyAction {
        LEFT, CREATED
    }
}
