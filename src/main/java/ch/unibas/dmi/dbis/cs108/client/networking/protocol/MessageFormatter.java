package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

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

    /**
     * Formats a name change request.
     *
     * @param playerId The username of the player.
     * @param newName  The new name of the player.
     * @return The formatted message.
     */
    public String formatNameChange(String playerId, String newName) {
        // ToDo: Implement Protocol for Name Change
        return null;
    }

    /**
     * Formats a disconnect notification.
     *
     * @param playerId The username of the player.
     * @return The formatted message.
     */
    public String formatDisconnect(String playerId) {
        return "EXIT:" + playerId;
    }

    /**
     * Formats a ping message.
     *
     * @param playerId The username of the player.
     * @return The formatted message.
     */
    public String formatPing(String playerId) {
        // ToDo: Implement Protocol for Ping Player ID and Time
        // return "PING:" + playerId + ";" + Instant.now().toEpochMilli();
        return "PING:";
    }

    /**
     * Formats a registration request.
     *
     * @param playerId   The username of the player.
     * @param playerName The name of the player.
     * @return The formatted message.
     */
    public String formatRegister(String playerId, String playerName) {
        return "JOIN:" + playerId + ";" + playerName;
    }
}
