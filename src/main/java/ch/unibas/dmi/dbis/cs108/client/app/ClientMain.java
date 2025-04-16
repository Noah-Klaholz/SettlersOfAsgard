package ch.unibas.dmi.dbis.cs108.client.app;

import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.NetworkController;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.PingFilter;

import java.util.logging.Logger;

/**
 * ClientMain class
 * This class is the main entry point for the client application.
 */
public class ClientMain {
    private static final Logger logger = Logger.getLogger(ClientMain.class.getName());

    /**
     * Main method
     * Entry point for the client application.
     *
     * @param args String[] Command line arguments
     */
    public static void main(String[] args) {
        NetworkController networkController = null;
        logger.setFilter(new PingFilter());

        try {
            if (args.length < 2) {
                logger.warning("Missing server address. Usage: java ClientMain <serverip>:<serverport> [username]");
                System.exit(1);
            }
            String[] serverAddress = args[1].split(":");
            if (serverAddress.length != 2) {
                logger.warning("Invalid server address. Expected: <serverip>:<serverport> [username]");
                System.exit(1);
            }

            int serverport = Integer.parseInt(serverAddress[1]);

            String username;
            if (args.length > 2) {
                username = args[2];
            } else {
                username = System.getProperty("user.name");
            }
            Player localPlayer = new Player(username);

            logger.info("Connecting to server at " + serverAddress[0] + ":" + serverport + " as " + username + "...");

            // Create network controller and connect
            networkController = new NetworkController(localPlayer);
            networkController.connect(serverAddress[0], serverport);

            if (!checkConnection(networkController)) return;

        } catch (Exception e) {
            logger.warning("Client start-up error: " + e.getMessage());
        } finally {
            if (networkController != null && networkController.isConnected()) {
                networkController.disconnect();
            }
            logger.info("Client terminated.");
        }
    }

    /**
     * Check if the client is connected to the server
     *
     * @param networkController NetworkController
     * @return boolean
     */
    private static boolean checkConnection(NetworkController networkController) {
        int attempts = 0;
        while (!networkController.isConnected() && attempts < 5) {
            try {
                Thread.sleep(500);
                attempts++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (!networkController.isConnected()) {
            logger.warning("Client failed to connect to server. Exiting...");
            return false;
        }
        return true;
    }
}