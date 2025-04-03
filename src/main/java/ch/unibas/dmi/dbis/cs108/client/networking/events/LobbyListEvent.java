package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

public class LobbyListEvent implements Event {
    private final Instant timestamp = Instant.now();
    private final String lobbies;

    public LobbyListEvent(String lobbies) {
        this.lobbies = lobbies;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }
    public String getLobbies() {
        return lobbies;
    }
}