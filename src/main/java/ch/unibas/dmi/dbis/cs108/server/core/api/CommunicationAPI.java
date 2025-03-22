package ch.unibas.dmi.dbis.cs108.server.core.api;

import ch.unibas.dmi.dbis.cs108.server.old.GameClient;

import java.util.logging.*;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
/**
 * Interface for the communication between the server and the client
 * Message String should be in the format "commandName:arg1,arg2,arg3"
 */
public interface CommunicationAPI {
    static final Logger logger = Logger.getLogger(GameClient.class.getName());

    /**
     * Filter for the logger to filter out ping messages to avoid clutter in the terminal
     */
    public class PingFilter implements Filter {
        @Override
        public boolean isLoggable(LogRecord record) {
            return !record.getMessage().contains("PING$");
        }
    }

    /**
     * Sends a message to the server
     * @param message the message to send
     */
    void sendMessage(String message);

    /**
     * Receives a message from the server
     */
    void processMessage(String received);

    /**
     * Utility class for network protocol constants
     */
    class NetworkProtocol {
        /**
         * Enum for network protocol constants (Command names)
         */
        public enum Commands {
            // administrative commands
            TEST("TEST"), // Test command
            SHUTDOWN("STDN"), // Shutdown command broadcast by server to disconnect all clients
            JOIN("JOIN"), // Player joins a game
            EXIT("EXIT"), // Player exits a game
            CHATGLOBAL("CHTG"), // Send a message to all players
            CHATPRIVATE("CHTP"), // Send a whisper message to only one player
            CREATELOBBY("CREA"), // Creates a new Lobby
            LISTLOBBIES("LIST"), // List all current Lobbies
            START("STRT"), // Start the game
            STATS("STAT"), // Request game state
            SYNCHRONIZE("SYNC"), // Request synchronization of the game
            // game mechanics
            STARTTURN("TURN"), // starts turn
            ENDTURN("ENDT"), // ends turn
            BUYHEXFIELD("BUYH"), // player buys a hexfield
            BUILDSTRUCTURE("BILD"), // player builds a structure
            UPGRADESTRUCTURE("UPGD"), // player upgrades a structure
            TRADERESOURCES("TRAD"), // player offers a trade of resources to another player
            RESOURCEBALANCE("BLNC"), // request the current resource balance of a player
            STARTRITUAL("RITU"), // player starts a ritual
            BLESSING("BLES"), // player gets blessed
            CURSE("CURS"), // player gets cursed
            USEARTIFACT("ARTF"), // player uses an artifact
            FINDARTIFACT("FIND"), // player finds an artifact
            // exception handling
            OK("OK"), // OK response
            ERROR("ERR"), // Error response
            PING("PING"); // Ping command

            private final String command;

            /**
             * Constructor for the enum
             * @param command
             */
            Commands(String command) {
                this.command = command;
            }

            /**
             * Getter for the command
             * @return the command
             * @see #command
             */
            public String getCommand() {
                return command;
            }

            /**
             * Returns the command enum from a command string
             * @param commandName
             * @return the command enum
             */
            public static Commands fromCommand(String commandName) {
                for(Commands cmd : values()) {
                    if(cmd.getCommand().equals(commandName)) {
                        return cmd;
                    }
                }
                throw new IllegalArgumentException("API-Unknown command: " + commandName);
            }
        }
    }
}

