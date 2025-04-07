package ch.unibas.dmi.dbis.cs108.client.ui.events;

/**
 * Event representing the connection status of the client.
 */
public class ConnectionStatusEvent {
    private final Status status;
    private final String message;

    /**
     * Constructor for ConnectionStatusEvent.
     *
     * @param status  The status of the connection.
     * @param message The message associated with the connection status.
     */
    public ConnectionStatusEvent(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Get the status of the connection.
     *
     * @return The connection status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Get the message associated with the connection status.
     *
     * @return The message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Enum representing the possible connection statuses.
     */
    public enum Status {
        /**
         * The client is connected to the server.
         */
        CONNECTED,
        /**
         * The client is in the process of connecting to the server.
         */
        CONNECTING,
        /**
         * The client is disconnected from the server.
         */
        DISCONNECTED
    }
}