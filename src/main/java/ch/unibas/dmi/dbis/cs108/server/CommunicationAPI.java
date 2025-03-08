package ch.unibas.dmi.dbis.cs108.server;

/**
 * Interface for the communication between the server and the client
 */
public interface CommunicationAPI {

    MessageParser messageParser = new MessageParser();

    /**
     * Sends a command to the server
     * @param message the message to send
     */
    void RecieveCommand(Command message);

    /**
     * Receives a command from the server
     * @return the message received
     */
    Command recieveCommand();
}
