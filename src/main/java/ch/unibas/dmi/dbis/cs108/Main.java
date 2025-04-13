package ch.unibas.dmi.dbis.cs108;

import ch.unibas.dmi.dbis.cs108.client.app.GameApplication;
import ch.unibas.dmi.dbis.cs108.server.app.ServerMain;

import java.util.logging.Logger;

/**
 * Main class for the server and the client, makes starting the server and client via terminal possible
 */
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * Main method for the server and the client
     *
     * @param args Usage in terminal:
     *             Server: java -jar xyz.jar server listenport
     *             Client: java -jar xyz.jar client serverip:serverport [username]
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            logger.info("Usage:");
            logger.info("Server: java -jar SettlersOfAsgard.jar server <listenport>");
            logger.info("Client: java -jar SettlersOfAsgard.jar client <serverip>:<serverport> [username]");
            System.exit(1);
        }

        String mode = args[0];

        if (mode.equalsIgnoreCase("server")) {
            ServerMain.main(args);
        } else if (mode.equalsIgnoreCase("client")) {
            GameApplication.main(args);
        } else {
            logger.warning("Invalid mode. Expected: 'server' or 'client'.");
            System.exit(1);
        }
    }
}
