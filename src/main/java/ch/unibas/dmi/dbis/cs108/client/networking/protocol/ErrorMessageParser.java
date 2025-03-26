package ch.unibas.dmi.dbis.cs108.client.networking.protocol;

import ch.unibas.dmi.dbis.cs108.client.networking.GameClient;
import ch.unibas.dmi.dbis.cs108.shared.protocol.ErrorsAPI;

import java.util.logging.Logger;

/**
 * Parses error messages received from the server.
 */
public class ErrorMessageParser {
    private static final Logger logger = Logger.getLogger(ErrorMessageParser.class.getName());
    /**
     * Parses an error message received from the server.
     *
     * @param rawMessage The raw message string received from the server.
     * @return The parsed error message.
     */
    public void parseErrorMessage(String rawMessage, GameClient client) {
        String[] parts = rawMessage.split("[$]", 2);
        String errorMessage = parts[1];

        ErrorsAPI.Errors err;
        try {
            err = ErrorsAPI.Errors.fromError(errorMessage);
        } catch (IllegalArgumentException e) {
            logger.warning("Protocol-Unknown error: " + errorMessage);
            return;
        }

        /**
         * Switch statement to handle the different error cases
         * Only log for unexpected errors
         * E.g. Player already exists is a normal error -> no logging!
         */
        switch(err) {
            case CANNOT_WHISPER_TO_SELF:
                logger.warning("Cannot whisper to self");
                break;
            case PLAYER_DOES_NOT_EXIST:
                logger.warning("Player does not exist");
                break;
            case NULL_MESSAGE_RECIEVED:
                logger.warning("Null message received");
                break;
            case LOBBY_CREATION_FAILED:
                logger.warning("Lobby creation failed");
                break;
            case JOIN_LOBBY_FAILED:
                break;
            case NOT_IN_LOBBY:
                break;
            case PLAYER_ALREADY_EXISTS:
                String[] args = errorMessage.split("[$]");
                String newPlayerName = args[1];
                client.setName(newPlayerName);
                break;
            case CANNOT_START_GAME:
                logger.warning("Cannot start game");
                break;
            default:
                logger.warning("Switch-Unknown error: " + errorMessage);
        }
    }
}
