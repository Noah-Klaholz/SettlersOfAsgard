package ch.unibas.dmi.dbis.cs108.client.networking.core;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for a network client that handles communication with a server.
 * Provides methods for connecting, disconnecting, sending messages, and
 * handling incoming messages.
 */
public interface NetworkClient {
    /**
     * Connects to the server.
     *
     * @param host Server host
     * @param port Server port
     * @return CompletableFuture that completes when connection is established.
     */
    CompletableFuture<Void> connect(String host, int port);

    /**
     * Disconnects from the server.
     */
    void disconnect();

    /**
     * Sends a message to the server.
     *
     * @param message Message to send
     * @return CompletableFuture that completes when the message is sent.
     */
    CompletableFuture<Void> send(String message);

    /**
     * Checks if the client is connected to the server.
     *
     * @return true if connected, false otherwise.
     */
    boolean isConnected();

    /**
     * Cleans up resources used by the client.
     */
    void cleanupResources();

    /**
     * Sets a message handler to process incoming messages.
     *
     * @param handler Message handler.
     */
    void setMessageHandler(MessageHandler handler);

    /**
     * Interface for handling incoming messages.
     */
    interface MessageHandler {
        void onMessage(String message);

        /**
         * Called when the client is disconnected from the server.
         *
         * @param cause The cause of the disconnection, if available.
         */
        void onDisconnect(Throwable cause);
    }
}
