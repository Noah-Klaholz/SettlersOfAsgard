package ch.unibas.dmi.dbis.cs108.server;

/**
 * Main class for the server and the client, makes starting the server and client via terminal possible
 */
public class ServerMain {

    /**
     * Main method for the server and the client
     * @param args
     * Usage in terminal:
     * Server: java -jar xyz.jar server <listenport>
     * Client: java -jar xyz.jar client <serverip>:<serverport>
     */
    public static void main(String[] args) {
        if(args.length < 2) {
            System.out.println("Usage:");
            System.out.println("Server: java -jar xyz.jar server <listenport>");
            System.out.println("Client: java -jar xyz.jar client <serverip>:<serverport>");
            System.exit(1);
        }

        String mode = args[0];

        if(mode.equalsIgnoreCase("server")) {
            try {
                int port = Integer.parseInt(args[1]);
                GameServer server = new GameServer(port);
                server.start();
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number in server mode.");
                System.exit(1);
            }
        } else if (mode.equalsIgnoreCase("client")) {
            String[] parts = args[1].split(":");
            if(parts.length != 2) {
                System.err.println("Invalid argument for client. Expected: <serverip>:<serverport>.");
                System.exit(1);
            }
            String serverIp = parts[0];
            try {
                int serverPort = Integer.parseInt(parts[1]);
                GameClient client = new GameClient(serverIp, serverPort);
                client.connect();
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number in client mode.");
                System.exit(1);
            }
        } else {
            System.err.println("Invalid mode. Expected: 'server' or 'client'.");
            System.exit(1);
        }

    }
}
