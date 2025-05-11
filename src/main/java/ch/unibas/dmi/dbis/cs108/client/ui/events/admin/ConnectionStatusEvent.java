package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.networking.events.ConnectionEvent.ConnectionState;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing a change in the network connection status.
 */
public class ConnectionStatusEvent implements UIEvent {

    /**
     * The connection state.
     */
    private final ConnectionState state;
    /**
     * An optional message associated with the connection status.
     */
    private final String message;

    /**
     * Constructs a new ConnectionStatusEvent.
     *
     * @param state   the connection state
     * @param message an optional message
     */
    public ConnectionStatusEvent(ConnectionState state, String message) {
        this.state = state;
        this.message = message;
    }

    /**
     * Gets the connection state.
     *
     * @return the connection state
     */
    public ConnectionState getState() {
        return state;
    }

    /**
     * Gets the status message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "CONNECTION_STATUS";
    }
}
