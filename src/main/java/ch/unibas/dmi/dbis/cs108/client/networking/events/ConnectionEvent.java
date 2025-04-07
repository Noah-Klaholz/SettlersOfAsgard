package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

/**
 * Event representing a connection state change.
 * This event is triggered when the connection state changes, such as connecting, connected, or disconnected.
 */
public class ConnectionEvent implements Event {
    /**
     * The timestamp of the event.
     */
    private final Instant timestamp = Instant.now();
    /**
     * The state of the connection.
     */
    private final ConnectionState state;
    /**
     * The message associated with the connection event.
     */
    private final String message;

    /**
     * Constructor for ConnectionEvent.
     *
     * @param state   The state of the connection.
     * @param message The message associated with the connection event.
     */
    public ConnectionEvent(ConnectionState state, String message) {
        this.state = state;
        this.message = message;
    }

    /**
     * Get the timestamp of the event.
     *
     * @return The timestamp.
     */
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get the state of the connection.
     *
     * @return The connection state.
     */
    public ConnectionState getState() {
        return state;
    }

    /**
     * Get the message associated with the connection event.
     *
     * @return The message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Enum representing the possible connection states.
     */
    public enum ConnectionState {
        /**
         * The connection is in the process of connecting.
         */
        CONNECTING,
        /**
         * The connection is established.
         */
        CONNECTED,
        /**
         * The connection is disconnected.
         */
        DISCONNECTED
    }
}