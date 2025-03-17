package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

public class MessageParser {
    public static void parse(String message) {
        if (message.startsWith("CHAT:")) {
            handleChatMessage(message);
        } else if (message.startsWith("UPDATE_BOARD")) {
            System.out.println("[Parser] Received board update: " + message);
        } else {
            System.out.println("[Parser] Unrecognized message: " + message);
        }
    }

    private static void handleChatMessage(String message) {

    }
}
