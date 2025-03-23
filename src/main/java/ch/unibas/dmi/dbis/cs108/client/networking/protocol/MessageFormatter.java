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

    //TODO Was macht das hier? -> Server überprüft automatisch, glaub das ist nicht nötig
    /**
     * Formats a disconnect notification.
     *
     * @param playerName The username of the player.
     * @return The formatted message.
     */
    public String formatDisconnect(String playerName) {
        try {
            return "EXIT$" + playerName; // Es gibt keinen Exit Command
        } catch (Exception e) {
            logger.severe("Failed to format disconnect: " + e.getMessage());
            return null;
        }
    }

    /**
     * Formats a ping message.
     *
     * @param playerName The username of the player.
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

    /**
     * Formats a pong message.
     * @param playerName
     * @return
     */
    public String formatPong(String playerName) {
        try {
            return "PONG$" + playerName;
        } catch (Exception e) {
            logger.severe("Failed to format pong: " + e.getMessage());
            return null;
        }
    }

    /**
     * Formats a create lobby message.
     * @param playerName The name of the player.
     * @param lobbyName The name of the lobby.
     * @return The formatted message.
     */
    public String formatCreateLobby(String playerName, String lobbyName) {
        try {
            return "CREA$" + playerName + "$" + lobbyName;
        } catch (Exception e) {
            logger.severe("Failed to format create lobby: " + e.getMessage());
            return null;
        }
    }

    /**
     * Formats a join lobby message.
     * @param playerName The name of the player.
     * @param lobbyName  The name of the lobby.
     * @return The formatted message.
     */
    public String formatJoinLobby(String playerName, String lobbyName) {
        try {
            return "JOIN$" + playerName + "$" + lobbyName;
        } catch (Exception e) {
            logger.severe("Failed to format join lobby: " + e.getMessage());
            return null;
        }
    }

    /**
     * Formats a leave lobby message.
     * @param playerName The name of the player.
     * @param lobbyName The name of the lobby.
     * @return The formatted message.
     */
    public String formatLeaveLobby(String playerName, String lobbyName) {
        try {
            return "EXIT$" + playerName + "$" + lobbyName;
        } catch (Exception e) {
            logger.severe("Failed to format leave lobby: " + e.getMessage());
            return null;
        }
    }

    /**
     * Formats a start game message.
     * @return The formatted message.
     */
    public String formatStartGame() {
        try {
            return "STRT$";
        } catch (Exception e) {
            logger.severe("Failed to format start game: " + e.getMessage());
            return null;
        }
    }

    /**
     * Formats a list lobbies message.
     * @return The formatted message.
     */
    public String formatListLobbies() {
        try {
            return "LIST$";
        } catch (Exception e) {
            logger.severe("Failed to format list lobbies: " + e.getMessage());
            return null;
        }
    }
}
