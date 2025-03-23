package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

/**
 * Formats protocol messages into human-readable display text
 */
public class DisplayFormatter {

    /**
     * Formats any protocol message for display
     *
     * @param rawMessage The raw protocol message
     * @return A user-friendly formatted message
     */
    public static String formatForDisplay(String rawMessage) {
        if (rawMessage == null) {
            return null;
        }

        if (rawMessage.startsWith("CHTG$")) {
            return formatChatMessage(rawMessage);
        } else if (rawMessage.startsWith("NOTF$")) {
            return formatNotification(rawMessage);
        } else if (rawMessage.startsWith("JOIN$")) {
            return formatJoinMessage(rawMessage);
        } else if (rawMessage.startsWith("LEAV$")) {
            return formatLeaveMessage(rawMessage);
        } else if (rawMessage.startsWith("ERR$")) {
            return formatErrorMessage(rawMessage);
        } else if (rawMessage.startsWith("LIST$")) {
            return formatLobbyList(rawMessage);
        } else if (rawMessage.startsWith("OK$")) {
            return formatSuccessMessage(rawMessage);
        }

        return rawMessage; // Default fallback
    }

    /**
     * Formats a chat Message to be displayed in the terminal correctly
     * @param rawMessage the raw message that gets transmitted
     * @return the formatted message
     */
    private static String formatChatMessage(String rawMessage) {
        String[] parts = rawMessage.split("\\$", 3);
        if (parts.length >= 3) {
            return parts[1] + ": " + parts[2];
        }
        return rawMessage;
    }

    /**
     * Formats a notification message (Info) to be displayed as such
     * @param rawMessage the raw message that gets transmitted
     * @return the formatted message
     */
    private static String formatNotification(String rawMessage) {
        return "Info: " + rawMessage.substring(5);
    }

    /**
     * Formats a join message
     * @param rawMessage the raw message that gets transmitted
     * @return the formatted message
     */
    private static String formatJoinMessage(String rawMessage) {
        String[] parts = rawMessage.split("\\$", 3);
        if (parts.length >= 3) {
            return parts[1] + " joined lobby: " + parts[2];
        }
        return rawMessage;
    }

    /**
     * Formats a leave-lobby message
     * @param rawMessage the raw message that gets transmitted
     * @return the formatted message
     */
    private static String formatLeaveMessage(String rawMessage) {
        String[] parts = rawMessage.split("\\$", 3);
        if (parts.length >= 3) {
            return parts[1] + " left lobby: " + parts[2];
        }
        return rawMessage;
    }

    /**
     * Formats error messages to be displayed as hints to the player
     * @param rawMessage the error message
     * @return the player hint
     */
    private static String formatErrorMessage(String rawMessage) {
        if(rawMessage.startsWith("ERR$106$PLAYER_ALREADY_EXISTS")) {
            return "Could not use System name as PlayerName. Please change your Name with /changeName.";
        } else if(rawMessage.startsWith("ERR$106$LOBBY_CREATION_FAILED")) {
            return "Could not create lobby, because a lobby with this name already exists. You can join an existing lobby with /join.";
        } else if(rawMessage.startsWith("ERR$106$JOIN_LOBBY_FAILED")) {
            return "Could not join lobby, because lobby does not exist. Create a new one with /create.";
        }
        return "Error: " + rawMessage.substring(4);
    }

    /**
     * Formats a lobby-List to be displayed
     * @param rawMessage the lobby-list-message
     * @return the formatted list
     */
    private static String formatLobbyList(String rawMessage) {
        return "Available lobbies: " + rawMessage.substring(5);
    }

    /**
     * Formats a success-message to be displayed
     * @param rawMessage the raw success-message
     * @return the formatted message
     */
    private static String formatSuccessMessage(String rawMessage) {
        return "Success: " + rawMessage.substring(3);
    }
}