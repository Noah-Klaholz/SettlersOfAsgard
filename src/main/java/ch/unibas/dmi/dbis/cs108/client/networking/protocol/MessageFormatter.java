package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

public class MessageFormatter {
    public static String formatChatMessage(String username, String message) {
        return "CHAT:" + username + ":" + message;
    }
}
