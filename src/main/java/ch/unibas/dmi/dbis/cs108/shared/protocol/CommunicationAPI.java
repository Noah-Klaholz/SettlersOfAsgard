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
        public enum Commands {
            // Test and system commands
            TEST("TEST"),        // Test command
            SHUTDOWN("STDN"),    // Shutdown command broadcast by server to disconnect all clients
            PING("PING"),        // Ping command to check connection

            // Player management commands
            REGISTER("RGST"),    // Register a new player
            CHANGENAME("CHAN"),  // Player changes their nickname
            EXIT("EXIT"),        // Player exits the game/disconnects

            // Lobby management commands
            CREATELOBBY("CREA"), // Creates a new lobby
            JOIN("JOIN"),        // Player joins a game/lobby
            LEAVE("LEAV"),       // Leave current lobby
            LISTLOBBIES("LIST"), // List all current lobbies
            LISTPLAYERS("LSTP"), // List all players in lobby/server (arg: LOBBY/SERVER)
            START("STRT"),       // Start the game

            // Chat commands
            CHATGLOBAL("CHTG"),  // Send a message to all players on server
            CHATLOBBY("CHTL"),   // Send a message to all players in the lobby
            CHATPRIVATE("CHTP"), // Send a whisper message to only one player

            // Game flow commands
            STARTTURN("TURN"),   // Starts a player's turn
            ENDTURN("ENDT"),     // Ends a player's turn
            STATS("STAT"),       // Request game state
            SYNCHRONIZE("SYNC"), // Request synchronization of the game
            GETGAMESTATUS("GSTS"), // Get detailed game status
            GETPRICES("GPRC"),   // Get in-game prices for actions/items

            // Game action commands
            BUYTILE("BUYT"),     // Player buys a tile at coordinates
            PLACESTRUCTURE("PLST"), // Player places a structure at coordinates
            USESTRUCTURE("USSR"), // Player uses a structure at coordinates
            UPGRADESTATUE("UPST"), // Player upgrades a statue at coordinates
            USESTATUE("USTA"),   // Player uses a statue at coordinates
            USEPLAYERARTIFACT("USPA"), // Player uses an artifact
            USETRAP("USTR"),     // Player uses a trap at coordinates

            // Legacy game mechanics (kept for backward compatibility)
            BUYHEXFIELD("BUYH"), // Player buys a hexfield
            BUILDSTRUCTURE("BILD"), // Player builds a structure
            UPGRADESTRUCTURE("UPGD"), // Player upgrades a structure
            USEARTIFACT("ARTF"), // Player uses an artifact (legacy version)

            // Response codes
            OK("OK"),           // OK response
            ERROR("ERR");       // Error response

            private final String command;

            /**
             * Constructor for the enum
             *
             * @param command
             */
            Commands(String command) {
                this.command = command;
            }

            /**
             * Returns the command enum from a command string
             *
             * @param commandName
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
             * @see #command
             */
            public String getCommand() {
                return command;
            }
        }
    }
}

