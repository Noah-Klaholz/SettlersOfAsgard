package ch.unibas.dmi.dbis.cs108.shared.protocol;

public class ErrorsAPI {

    /**
     * Enum for the different errors that can be sent by the server
     */
    public enum Errors {
        PLAYER_ALREADY_EXISTS("ERR$106$PLAYER_ALREADY_EXISTS"),
        CANNOT_START_GAME("ERR$106$CANNOT_START_GAME"),
        NOT_IN_LOBBY("ERR$106$NOT_IN_LOBBY"),
        JOIN_LOBBY_FAILED("ERR$106$JOIN_LOBBY_FAILED"),
        LOBBY_CREATION_FAILED("ERR$106$LOBBY_CREATION_FAILED"),
        NULL_MESSAGE_RECIEVED("ERR0R$103$Null");

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