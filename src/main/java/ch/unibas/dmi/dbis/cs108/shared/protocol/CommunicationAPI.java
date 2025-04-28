package ch.unibas.dmi.dbis.cs108.shared.protocol;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * Interface for the communication between the server and the client
 * Message String should be in the format "commandName:arg1,arg2,arg3"
 */
public interface CommunicationAPI {
    /**
     * Sends a message to the server
     *
     * @param message the message to send
     */
    void sendMessage(String message);

    /**
     * Receives a message from the server
     *
     * @param received the message received
     */
    void processMessage(String received);

    /**
     * Filter for the logger to filter out ping messages to avoid clutter in the terminal
     */
    class PingFilter implements Filter {
        @Override
        public boolean isLoggable(LogRecord record) {
            return !record.getMessage().contains("PING$");
        }
    }

    /**
     * Utility class for network protocol constants
     */
    class NetworkProtocol {
        /**
         * Enum for network protocol constants (Command names)
         */
        /**
         * Enum for network protocol constants (Command names)
         */
        public enum Commands {
            // Test and system commands
            /**
             * Test command
             */
            TEST("TEST"),
            /**
             * Shutdown command broadcast by server to disconnect all clients
             */
            SHUTDOWN("STDN"),
            /**
             * Ping command to check connection
             */
            PING("PING"),

            // Player management commands
            /**
             * Register a new player
             */
            REGISTER("RGST"),
            /**
             * Player changes their nickname
             */
            CHANGENAME("CHAN"),
            /**
             * Player exits the game/disconnects
             */
            EXIT("EXIT"),

            // Lobby management commands
            /**
             * Creates a new lobby
             */
            CREATELOBBY("CREA"),
            /**
             * Player joins a game/lobby
             */
            JOIN("JOIN"),
            /**
             * Leave current lobby
             */
            LEAVE("LEAV"),
            /**
             * List all current lobbies
             */
            LISTLOBBIES("LIST"),
            /**
             * List all players in lobby/server (arg: LOBBY/SERVER)
             */
            LISTPLAYERS("LSTP"),
            /**
             * Start the game
             */
            START("STRT"),

            // Chat commands
            /**
             * Send a message to all players on server
             */
            CHATGLOBAL("CHTG"),
            /**
             * Send a message to all players in the lobby
             */
            CHATLOBBY("CHTL"),
            /**
             * Send a whisper message to only one player
             */
            CHATPRIVATE("CHTP"),

            // Game flow commands
            /**
             * Starts a player's turn
             */
            STARTTURN("TURN"),
            /**
             * Ends a player's turn
             */
            ENDTURN("ENDT"),
            /**
             * Ends the game and shows the final score
             */
            ENDGAME("ENDG"),
            /**
             * Request synchronization of the game
             */
            SYNCHRONIZE("SYNC"),
            /**
             * Get detailed game status
             */
            GETGAMESTATUS("GSTS"),
            /**
             * Get in-game prices for actions/items
             */
            GETPRICES("GPRC"),

            // Game action commands
            /**
             * Player buys a tile at coordinates
             */
            BUYTILE("BUYT"),
            /**
             * Player buys a statue at coordinates
             */
            BUYSTATUE("BYST"),
            /**
             * Player buys a Structure with structure ID
             */
            BUYSTRUCTURE("BUST"),
            /**
             * Player places a structure at coordinates
             */
            PLACESTRUCTURE("PLST"),
            /**
             * Player uses a structure at coordinates
             */
            USESTRUCTURE("USSR"),
            /**
             * Player places a statue at coordinates
             */
            PLACESTATUE("PLSU"),
            /**
             * Player upgrades a statue at coordinates
             */
            UPGRADESTATUE("UPST"),
            /**
             * Player uses a statue at coordinates
             */
            USESTATUE("USTA"),
            /**
             * Player uses an artifact
             */
            USEPLAYERARTIFACT("USPA"),
            /**
             * Player uses a field artifact
             */
            USEFIELDARTIFACT("USFA"),
            /**
             * Player claims all tiles (cheat code)
             */
            CLAIMALL("CLAM"),
            /**
             * Player disconnects from the game
             */
            DISCONNECT("DISC"),
            /**
             * Entity notifies player
             */
            INFO("INFO"),
            /**
             *
             */
            LEADERBOARD("LEAD"),

            // Response codes
            /**
             * OK response
             */
            OK("OK"),
            /**
             * Error response
             */
            ERROR("ERR");

            private final String command;

            /**
             * Constructor for the enum
             *
             * @param command the command string
             */
            Commands(String command) {
                this.command = command;
            }

            /**
             * Returns the command enum from a command string
             *
             * @param commandName the command name
             * @return the command enum
             */
            public static Commands fromCommand(String commandName) {
                for (Commands cmd : values()) {
                    if (cmd.getCommand().equals(commandName)) {
                        return cmd;
                    }
                }
                throw new IllegalArgumentException("API-Unknown command: " + commandName);
            }

            /**
             * Getter for the command
             *
             * @return the command
             */
            public String getCommand() {
                return command;
            }
        }
    }
}

