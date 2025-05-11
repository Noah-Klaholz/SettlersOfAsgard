package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

/**
 * UIEvent representing a connection state change.
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
     * True if the event is regarding the client, false if its about other players.
     */
    private boolean isSelf;

    /**
     * Constructor for ConnectionEvent.
     *
     * @param state   The state of the connection.
     * @param message The message associated with the connection event.
     *  * @param isSelf  True if the event is regarding the client, false if its about other players.
     */
    public ConnectionEvent(ConnectionState state, String message, boolean isSelf) {
        this.state = state;
        this.message = message;
        this.isSelf = isSelf;
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
     * Gets the current value of isSelf.
     *
     * @return the value of isSelf.
     */
    public boolean isSelf() {
        return isSelf;
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