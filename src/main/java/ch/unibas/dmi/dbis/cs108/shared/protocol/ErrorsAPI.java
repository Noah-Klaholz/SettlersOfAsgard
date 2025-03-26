package ch.unibas.dmi.dbis.cs108.shared.protocol;

/**
 * Class for the errors that can be sent by the server
 */
public class ErrorsAPI {

    /**
     * Enum for the different errors that can be sent by the server
     */
    public enum Errors {
        PLAYER_ALREADY_EXISTS("106$PLAYER_ALREADY_EXISTS"),
        PLAYER_DOES_NOT_EXIST("106$PLAYER_DOES_NOT_EXIST"),
        CANNOT_WHISPER_TO_SELF("106$CANNOT_WHISPER_TO_SELF"),
        CANNOT_START_GAME("106$CANNOT_START_GAME"),
        NOT_IN_LOBBY("106$NOT_IN_LOBBY"),
        JOIN_LOBBY_FAILED("106$JOIN_LOBBY_FAILED"),
        LOBBY_CREATION_FAILED("106$LOBBY_CREATION_FAILED"),
        NULL_MESSAGE_RECIEVED("103$Null");

    private final String error;

        /**
         * Constructor for the enum
         * @param error
         */
        Errors(String error) {
            this.error = error;
        }

        /**
         * Getter for the command
         * @return the command
         */
        public String getError() {
            return error;
        }

        /**
         * Returns the command enum from a command string
         * @param errorName
         * @return the command enum
         */
        public static Errors fromError(String errorName) {
            for(Errors err : values()) {
                if(err.getError().equals(errorName)) {
                    return err;
                }
            }
            throw new IllegalArgumentException("API-Unknown command: " + errorName);
        }
    }
}