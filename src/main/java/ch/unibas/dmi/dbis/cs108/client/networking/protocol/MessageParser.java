package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

/**
 * This class provides static methods to parse messages according to the protocol.
 */
public class MessageParser {
    /**
     * Parses a message and handles it accordingly.
     * @param message The message to be parsed.
     */
    public static void parse(String message) {
        if (message.startsWith("CHAT:")) {
            handleChatMessage(message);
        } else if (message.startsWith("UPDATE_BOARD")) {
            System.out.println("[Parser] Received board update: " + message);
        } else {
            System.out.println("[Parser] Unrecognized message: " + message);
        }
    }

    /**
     * Handles a chat message.
     * @param message The chat message to be handled.
     */
    private static void handleChatMessage(String message) {
        String[] parts = message.split(":", 3);
        if (parts.length == 3) {
            String username = parts[1];
            String chatMessage = parts[2];
            System.out.println(username + ": " + chatMessage);
        }
    }
}
