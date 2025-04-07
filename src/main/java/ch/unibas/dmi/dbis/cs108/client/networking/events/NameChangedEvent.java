package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

public class NameChangedEvent implements Event {
    private final Instant timestamp = Instant.now();
    private final String newName;

    public NameChangedEvent(String newName) {
        this.newName = newName;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public String getNewName() {
        return newName;
    }
}