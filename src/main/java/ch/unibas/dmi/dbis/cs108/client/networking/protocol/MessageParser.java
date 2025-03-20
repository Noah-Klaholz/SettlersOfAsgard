package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

/**
 * This class provides static methods to parse messages according to the protocol.
 */
public class MessageParser {

    public String parseChatMessage(String rawMessage){
        return null;
    }

    public String parseRegistrationResponse(String rawMessage){
        return null;
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
