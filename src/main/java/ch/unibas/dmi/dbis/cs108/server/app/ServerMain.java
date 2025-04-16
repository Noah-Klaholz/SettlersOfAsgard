package ch.unibas.dmi.dbis.cs108.server.app;

import ch.unibas.dmi.dbis.cs108.server.networking.GameServer;

import java.util.logging.Logger;

/**
 * Main class for starting the Server
 */
public class ServerMain {

    /** Logger to log logging */
    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());

    /**
     * Main method for the server
     * Starts the server and adds a shutdown hook
     *
     * @param args contains the portNr at index 1.
     */
    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[1]);
            GameServer server = new GameServer(port);
            new Thread(server::start).start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Server shutting down...");
                server.shutdown();
            }));
        } catch (NumberFormatException e) {
            logger.info("Invalid port number in server mode.");
            System.exit(1);
        }
    }
}
