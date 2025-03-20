package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

import java.time.Instant;

/**
 * This class provides static methods to format messages according to the protocol.
 */
public class MessageFormatter {
    /**
     * Formats a chat message.
     *
     * @param playerId The username of the sender.
     * @param message  The message to be sent.
     * @return The formatted message.
     */
    public static String formatChatMessage(String playerId, String message) {
        return "CHTG:" + playerId + ";" + message;
    }

    public String formatNameChange(String playerId, String newName) {
        // ToDo: Implement Protocol for Name Change
        return null;
    }

    public String formatDisconnect(String playerId) {
        return "EXIT:" + playerId;
    }

    public String formatPing(String playerId) {
        // ToDo: Implement Protocol for Ping Player ID and Time
        // return "PING:" + playerId + ";" + Instant.now().toEpochMilli();
        return "Ping:";
    }
}
