package ch.unibas.dmi.dbis.cs108.server;

/**
 * Interface for the communication between the server and the client
 * Message String should be in the format "commandName:arg1,arg2,arg3"
 */
public interface CommunicationAPI {
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
        // administrative commands
        public final static String TEST = "TEST"; // Test command
        public final static String SHUTDOWN = "STDN"; // Shutdown command broadcast by server to disconnect all clients
        public final static String JOIN = "JOIN"; // Player joins a game
        public final static String EXIT = "EXIT"; // Player exits a game
        public final static String CHATGLOBAL = "CHTG"; // Send a message to all players
        public final static String CHATPRIVATE = "CHTP"; // Send a whisper message to only one player
        public final static String LISTLOBBIES = "LIST"; // List all current Lobbies
        public final static String START = "STRT"; // Start the game
        public final static String STATS = "STAT"; // Request game state
        public final static String SYNCHRONIZE = "SYNC"; // Request synchronization of the game
        // game mechanics
        public final static String STARTTURN = "TURN"; // starts turn
        public final static String ENDTURN = "ENDT"; // ends turn
        public final static String BUYHEXFIELD = "BUYH"; // player buys a hexfield
        public final static String BUILDSTRUCTURE = "BILD"; // player builds a structure
        public final static String UPGRADESTRUCTURE = "UPGD"; // player upgrades a structure
        public final static String TRADERESOURCES = "TRAD"; // player offers a trade of resources to another player
        public final static String RESOURCEBALANCE = "BLNC"; // request the current resource balance of a player
        public final static String STARTRITUAL = "RITU"; // player starts a ritual
        public final static String BLESSING = "BLES"; // player gets blessed
        public final static String CURSE = "CURS"; // player gets cursed
        public final static String USEARTIFACT = "ARTF"; // player uses an artifact
        public final static String FINDARTIFACT = "FIND"; // player finds an artifact
        // exception handling
        public final static String OK = "OK"; // OK response
        public final static String ERROR = "ERR"; // Error response

    }
}

