package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

import ch.unibas.dmi.dbis.cs108.client.networking.GameClient;

public class ErrorMessageParser {

    /**
     * Parses an error message received from the server.
     *
     * @param rawMessage The raw message string received from the server.
     * @return The parsed error message.
     */
    public void parseErrorMessage(String rawMessage, GameClient client) {
        String[] parts = rawMessage.split("[$]", 2);
        String errorMessage = parts[1];
    }
}
