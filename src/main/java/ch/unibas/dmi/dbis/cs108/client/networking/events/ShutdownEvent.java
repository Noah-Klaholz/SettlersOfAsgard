package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

public class ShutdownEvent implements Event {
    private final String reason;
    private final Instant timestamp = Instant.now();

    public ShutdownEvent(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }
}
