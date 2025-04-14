package ch.unibas.dmi.dbis.cs108.shared.protocol;

/**
 * Class for the errors that can be sent by the server
 */
public class ErrorsAPI {

    /**
     * Enum for the different errors that can be sent by the server
     */
    public enum Errors {
        /**
         * Error code for failed game command execution
         */
        GAME_COMMAND_FAILED("106$GAME_COMMAND_FAILED"),
        /**
         * Error code player already exists
         */
        PLAYER_ALREADY_EXISTS("106$PLAYER_ALREADY_EXISTS"),
        /**
         * Error code player does not exist
         */
        PLAYER_DOES_NOT_EXIST("106$PLAYER_DOES_NOT_EXIST"),
        /**
         * Error code cannot whisper to self
         */
        CANNOT_WHISPER_TO_SELF("106$CANNOT_WHISPER_TO_SELF"),
        /**
         * Error code cannot start game
         */
        CANNOT_START_GAME("106$CANNOT_START_GAME"),
        /**
         * Error code not in lobby
         */
        NOT_IN_LOBBY("106$NOT_IN_LOBBY"),
        /**
         * Error code not your turn
         */
        NOT_PLAYER_TURN("106$NOT_PLAYER_TURN"),
        /**
         * Error code join lobby failed
         */
        JOIN_LOBBY_FAILED("106$JOIN_LOBBY_FAILED"),
        /**
         * Error code lobby creation failed
         */
        LOBBY_CREATION_FAILED("106$LOBBY_CREATION_FAILED"),
        /**
         * Error code NullPointer
         */
        NULL_MESSAGE_RECIEVED("103$NULL_MESSAGE_RECIEVED"),
        /**
         * Error message for unhandled command
         */
        UNHANDLED_COMMAND("106$UNHANDLED_COMMAND"),
        /**
         * Error code for invalid parameters -> append commandname to the error message
         */
        INVALID_PARAMETERS("106$INVALID_PARAMETERS"),
        /**
         * Error code for invalid commands
         */
        INVALID_COMMAND("106$INVALID_COMMAND"),
        /**
         * Error message for unknown command
         */
        UNKNOWN_COMMAND("106$UNKNOWN_COMMAND");

        private final String error;

        /**
         * Constructor for the enum
         *
         * @param error
         */
        Errors(String error) {
            this.error = error;
        }

        /**
         * Returns the command enum from a command string
         *
         * @param errorName the command string
         * @return the command enum
         */
        public static Errors fromError(String errorName) {
            for (Errors err : values()) {
                if (err.getError().equals(errorName)) {
                    return err;
                }
            }
            throw new IllegalArgumentException("API-Unknown command: " + errorName);
        }

        /**
         * Getter for the command
         *
         * @return the command
         */
        public String getError() {
            return error;
        }
    }
}