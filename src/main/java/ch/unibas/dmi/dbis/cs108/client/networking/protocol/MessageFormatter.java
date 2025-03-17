package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

/**
 * This class provides static methods to format messages according to the protocol.
 */
public class MessageFormatter {
    /**
     * Formats a chat message.
     * @param username The username of the sender.
     * @param message The message to be sent.
     * @return The formatted message.
     */
    public static String formatChatMessage(String username, String message) {
        return "CHAT:" + username + ":" + message;
    }
}
