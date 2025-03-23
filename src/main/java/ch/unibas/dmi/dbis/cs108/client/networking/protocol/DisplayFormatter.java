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

    private static String formatChatMessage(String rawMessage) {
        String[] parts = rawMessage.split("\\$", 3);
        if (parts.length >= 3) {
            return parts[1] + ": " + parts[2];
        }
        return rawMessage;
    }

    private static String formatNotification(String rawMessage) {
        return "Info: " + rawMessage.substring(5);
    }

    private static String formatJoinMessage(String rawMessage) {
        String[] parts = rawMessage.split("\\$", 3);
        if (parts.length >= 3) {
            return parts[1] + " joined lobby: " + parts[2];
        }
        return rawMessage;
    }

    private static String formatLeaveMessage(String rawMessage) {
        String[] parts = rawMessage.split("\\$", 3);
        if (parts.length >= 3) {
            return parts[1] + " left lobby: " + parts[2];
        }
        return rawMessage;
    }

    private static String formatErrorMessage(String rawMessage) {
        return "Error: " + rawMessage.substring(4);
    }

    private static String formatLobbyList(String rawMessage) {
        return "Available lobbies: " + rawMessage.substring(5);
    }

    private static String formatSuccessMessage(String rawMessage) {
        return "Success: " + rawMessage.substring(3);
    }
}