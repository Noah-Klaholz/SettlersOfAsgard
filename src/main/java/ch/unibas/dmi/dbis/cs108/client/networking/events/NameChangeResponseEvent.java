package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

public class NameChangeResponseEvent implements Event {
    private final boolean success;
    private final String newName;
    private final String message;

    public NameChangeResponseEvent(boolean success, String newName, String message) {
        this.success = success;
        this.newName = newName;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getNewName() {
        return newName;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public Instant getTimestamp() {
        return null;
    }
}