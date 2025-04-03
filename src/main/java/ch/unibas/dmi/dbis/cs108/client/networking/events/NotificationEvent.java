package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

public class NotificationEvent implements Event {
    private final Instant timestamp = Instant.now();
    private final String message;

    public NotificationEvent(String message) {
        this.message = message;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}