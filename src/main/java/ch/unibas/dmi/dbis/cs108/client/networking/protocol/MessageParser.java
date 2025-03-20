package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

/**
 * This class provides static methods to parse messages according to the protocol.
 */
public class MessageParser {

    public String parseChatMessage(String rawMessage){
        String[] parts = rawMessage.split("[:;]", 3);
        if (parts.length >= 3) {
            return parts[1] + ": " + parts[2];
        }
        return "Invalid chat message format";
    }

    public String parseRegistrationResponse(String rawMessage){
        String[] parts = rawMessage.split("[:;]", 2);
        if (parts.length == 2) {
            return parts[1];
        }
        return "Unknown";
    }

    public long parsePingResponse(String rawMessage) {
        String[] parts = rawMessage.split("[:;]", 2);
        if (parts.length == 2) {
            try {
                return Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
