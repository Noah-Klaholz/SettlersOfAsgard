package ch.unibas.dmi.dbis.cs108.server;

import java.util.logging.Logger;

/**
 * Main class for the server and the client, makes starting the server and client via terminal possible
 */
public class ServerMain {

    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());

    /**
     * Main method for the server and the client
     * @param args
     * Usage in terminal:
     * Server: java -jar xyz.jar server <listenport>
     * Client: java -jar xyz.jar client <serverip>:<serverport>
     */
    public static void main(String[] args) {
        if(args.length < 2) {
            logger.info("Usage:");
            logger.info("Server: java -jar xyz.jar server <listenport>"); //TODO change project name and change xyz to the new name
            logger.info("Client: java -jar xyz.jar client <serverip>:<serverport>"); //TODO change project name and change xyz to the new name
            System.exit(1);
        }

        String mode = args[0];

        if (mode.equalsIgnoreCase("server")) {
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
        } else if (mode.equalsIgnoreCase("client")) {
            String[] parts = args[1].split(":");
            if(parts.length != 2) {
                logger.warning("Invalid argument for client. Expected: <serverip>:<serverport>.");
                System.exit(1);
            }
            String serverIp = parts[0];
            try {
                int serverPort = Integer.parseInt(parts[1]);
                GameClient client = new GameClient(serverIp, serverPort);
                client.connect();
                client.start();
            } catch (NumberFormatException e) {
                logger.warning("Invalid port number in client mode.");
                System.exit(1);
            }
        } else {
            logger.warning("Invalid mode. Expected: 'server' or 'client'.");
            System.exit(1);
        }
    }
}
