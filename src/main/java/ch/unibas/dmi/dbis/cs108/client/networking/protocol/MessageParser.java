package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

/**
 * Parses messages received from the server
 */
public class MessageParser {

    /**
     * Parses a chat message received from the server.
     *
     * @param rawMessage The raw message string received from the server.
     * @return The parsed chat message in the format "sender: message".
     */
    public String parseChatMessage(String rawMessage) {
        String[] parts = rawMessage.split("[$]", 3);
        if (parts.length >= 3) {
            return parts[1] + ": " + parts[2];
        }
        return "Invalid chat message format: " + rawMessage;

    }

    /**
     * Parses a registration response received from the server.
     *
     * @param rawMessage The raw message string received from the server.
     * @return The parsed registration response.
     */
    public String parseRegistrationResponse(String rawMessage) {
        String[] parts = rawMessage.split("[$]", 2);
        if (parts.length == 2) {
            return parts[1];
        }
        return "Unknown";
    }

    /**
     * Parses a ping response received from the server.
     *
     * @param rawMessage The raw message string received from the server.
     * @return The timestamp of the ping response.
     */
    public long parsePingResponse(String rawMessage) {
        String[] parts = rawMessage.split("[$]", 2);
        if (parts.length == 2) {
            try {
                return Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public String parseErrorResponse(String rawMessage) {
        if(rawMessage.startsWith("ERR$106$PLAYER_ALREADY_EXISTS")) {
            return "Player already exists. Change your name with /changeName";
        }
        return "Server sent Error message: " + rawMessage;
    }
}
