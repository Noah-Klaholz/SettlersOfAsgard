package ch.unibas.dmi.dbis.cs108.server;

/**
 * Interface for the communication between the server and the client
 * Message String should be in the format "commandName:arg1,arg2,arg3"
 */
public interface CommunicationAPI {
    /**
     * Sends a message to the server
     * @param message the message to send
     */
    void sendMessage(String message);

    /**
     * Receives a message from the server
     */
    void processMessage(String received);

    /**
     * Utility class for network protocol constants
     */
    class NetworkProtocol {
        public final static String TEST = "TEST"; // Test command
        public final static String SHUTDOWN = "STDN"; // Shutdown command broadcast by server to disconnect all clients
        public final static String OK = "OK"; // OK response
        public final static String ERROR = "ERR"; // Error response
    }
}

