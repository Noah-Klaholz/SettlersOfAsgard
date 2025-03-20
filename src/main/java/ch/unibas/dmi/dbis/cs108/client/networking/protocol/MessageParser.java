package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

/**
 * This class provides static methods to parse messages according to the protocol.
 */
public class MessageParser {

    public String parseChatMessage(String rawMessage){
        String[] parts = rawMessage.split("[:;]", 3);
        if (parts.length >= 3) {
            return parts[1] + ": " + parts[2];
        }
        return "Invalid chat message format";
    }

    public String parseRegistrationResponse(String rawMessage){
        String[] parts = rawMessage.split("[:;]", 2);
        if (parts.length == 2) {
            return parts[1];
        }
        return "Unknown";
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
