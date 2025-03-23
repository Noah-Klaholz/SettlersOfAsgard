package ch.unibas.dmi.dbis.cs108.shared.protocol;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * Interface for the communication between the server and the client.
 * Message String should be in the format "commandName:arg1,arg2,arg3".
 */
public interface CommunicationAPI {

    /**
     * Filter for the logger to filter out ping messages to avoid clutter in the terminal.
     */
    class PingFilter implements Filter {
        @Override
        public boolean isLoggable(LogRecord record) {
            return !record.getMessage().contains("PING$");
        }
    }

    /**
     * Sends a message to the server.
     * @param message the message to send.
     */
    void sendMessage(String message);

    /**
     * Receives a message from the server.
     * @param received the received message.
     */
    void processMessage(String received);

    /**
     * Utility class for network protocol constants.
     */
    class NetworkProtocol {

        /**
         * Enum for network protocol commands.
         */
        public enum Commands {
            // Administrative commands
            TEST("TEST"),
            SHUTDOWN("STDN"),
            JOIN("JOIN"),
            EXIT("EXIT"),
            CHATGLOBAL("CHTG"),
            CHATPRIVATE("CHTP"),
            CREATELOBBY("CREA"),
            LISTLOBBIES("LIST"),
            START("STRT"),
            STATS("STAT"),
            SYNCHRONIZE("SYNC"),
            CHANGENAME("CHAN"),
            REGISTER("RGST"),
            // Game mechanics
            STARTTURN("TURN"),
            ENDTURN("ENDT"),
            BUYHEXFIELD("BUYH"),
            BUILDSTRUCTURE("BILD"),
            UPGRADESTRUCTURE("UPGD"),
            TRADERESOURCES("TRAD"),
            RESOURCEBALANCE("BLNC"),
            STARTRITUAL("RITU"),
            BLESSING("BLES"),
            CURSE("CURS"),
            USEARTIFACT("ARTF"),
            FINDARTIFACT("FIND"),
            // Exception handling
            OK("OK"),
            ERROR("ERR"),
            PING("PING");

            private final String command;

            Commands(String command) {
                this.command = command;
            }

            public String getCommand() {
                return command;
            }

            public static Commands fromCommand(String commandName) {
                for (Commands cmd : values()) {
                    if (cmd.getCommand().equals(commandName)) {
                        return cmd;
                    }
                }
                throw new IllegalArgumentException("API-Unknown command: " + commandName);
            }
        }
    }
}