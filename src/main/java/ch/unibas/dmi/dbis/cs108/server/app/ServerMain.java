package ch.unibas.dmi.dbis.cs108.server.app;

import ch.unibas.dmi.dbis.cs108.server.networking.GameServer;

import java.util.logging.Logger;

/**
 * Main class for starting the Server
 */
public class ServerMain {

    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());

    /**
     * Main method for the server
     * Starts the server and adds a shutdown hook
     * @param portNr String indicating the port number
     */
    public static void main(String portNr) {
        try {
            int port = Integer.parseInt(portNr);
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
