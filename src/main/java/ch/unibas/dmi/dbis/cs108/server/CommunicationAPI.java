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
     * @return the message received
     */
    String receiveMessage();
}
