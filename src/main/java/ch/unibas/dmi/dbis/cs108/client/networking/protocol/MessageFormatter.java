package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

import java.util.logging.Logger;
/**
 * This class provides static methods to format messages according to the protocol.
 */
public class MessageFormatter {
    private static final Logger logger = Logger.getLogger(MessageFormatter.class.getName());

    /**
     * Formats a chat message.
     *
     * @param playerName The username of the sender.
     * @param message  The message to be sent.
     * @return The formatted message.
     */
    public static String formatChatMessage(String playerName, String message) {
        try {
            return "CHTG$" + playerName + "$" + message;
        } catch (Exception e) {
            logger.severe("Failed to format chat message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Formats a name change request.
     *
     * @param playerId The username of the player.
     * @param newName  The new name of the player.
     * @return The formatted message.
     */
    public String formatNameChange(String newName) {
        try {
            return "CHAN$" +  newName;
        } catch (Exception e) {
            logger.severe("Failed to format name change: " + e.getMessage());
            return null;
        }
    }

    /**
     * Formats a disconnect notification.
     *
     * @param playerId The username of the player.
     * @return The formatted message.
     */
    public String formatDisconnect(String playerName) {
        try {
            return "EXIT$" + playerName;
        } catch (Exception e) {
            logger.severe("Failed to format disconnect: " + e.getMessage());
            return null;
        }
    }

    /**
     * Formats a ping message.
     *
     * @param playerId The username of the player.
     * @return The formatted message.
     */
    public String formatPing(String playerName) {
        try {
            // ToDo: Implement Protocol for Ping Player ID and Time
            // return "PING:" + playerId + ";" + Instant.now().toEpochMilli();
            return "PING$" + playerName;
        } catch (Exception e) {
            logger.severe("Failed to format ping: " + e.getMessage());
            return null;
        }
    }

    /**
     * Formats a registration request.
     *
     * @param playerId   The username of the player.
     * @param playerName The name of the player.
     * @return The formatted message.
     */
    public String formatRegister(String playerName) {
        try {
            return "RGST$" + playerName;
        } catch (Exception e) {
            logger.severe("Failed to format register: " + e.getMessage());
            return null;
        }
    }

    public String formatPong(String playerName) {
        try {
            return "PONG$" + playerName;
        } catch (Exception e) {
            logger.severe("Failed to format pong: " + e.getMessage());
            return null;
        }
    }

    public String formatCreateLobby(String playerName, String lobbyName) {
        try {
            return "CREA$" + playerName + "$" + lobbyName;
        } catch (Exception e) {
            logger.severe("Failed to format create lobby: " + e.getMessage());
            return null;
        }
    }

    public String formatJoinLobby(String playerName, String lobbyName) {
        try {
            return "JOIN$" + playerName + "$" + lobbyName;
        } catch (Exception e) {
            logger.severe("Failed to format join lobby: " + e.getMessage());
            return null;
        }
    }
}
