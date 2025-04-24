package ch.unibas.dmi.dbis.cs108.client.networking;

/**
 * Represents the possible states of the client's network connection to the
 * server.
 */
public enum ConnectionState {
    /**
     * The client is actively connected to the server.
     */
    CONNECTED,

    /**
     * The client is not connected to the server.
     */
    DISCONNECTED
    // Future states like CONNECTING, RECONNECTING could be added here.
}
