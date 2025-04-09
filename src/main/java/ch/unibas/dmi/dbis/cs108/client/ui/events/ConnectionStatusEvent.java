package ch.unibas.dmi.dbis.cs108.client.ui.events;


import ch.unibas.dmi.dbis.cs108.client.networking.events.ConnectionEvent.ConnectionState;
/**
 * Event representing the connection status of the client.
 */
public class ConnectionStatusEvent {
    private final ConnectionState status;
    private final String message;

    /**
     * Constructor for ConnectionStatusEvent.
     *
     * @param status  The status of the connection.
     * @param message The message associated with the connection status.
     */
    public ConnectionStatusEvent(ConnectionState status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Get the status of the connection.
     *
     * @return The connection status.
     */
    public ConnectionState getStatus() {
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

}