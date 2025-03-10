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
    void processMessage(String received);

    /**
     * The Command class is responsible for parsing messages into commands.
     */
    class NetworkProtocol {
        /**
         * Parses a message into a command.
         * @param cmd The message to parse
         */
        public static void processCommand(Command cmd) {
            //TODO implement command handling here
            if(cmd.isValid()) {
                switch(cmd.getCommand()) {
                    case "TEST":
                        System.out.println("TEST");
                        break;
                    default:
                        System.err.println("Unknown command: " + cmd.getCommand());
                }
            } else {
                System.err.println("Invalid command: " + cmd);
            }
        }
    }
}

