package ch.unibas.dmi.dbis.cs108.client.ui.events;

public class ConnectionStatusEvent {
    private final Status status;
    private final String message;
    public ConnectionStatusEvent(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public enum Status {
        CONNECTED, CONNECTING, DISCONNECTED
    }
}