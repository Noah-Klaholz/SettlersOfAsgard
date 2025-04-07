package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

public class ConnectionEvent implements Event {
    private final Instant timestamp = Instant.now();
    private final ConnectionState state;
    private final String message;

    public ConnectionEvent(ConnectionState state, String message) {
        this.state = state;
        this.message = message;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public ConnectionState getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }

    public enum ConnectionState {
        CONNECTING, CONNECTED, DISCONNECTED
    }
}