package ch.unibas.dmi.dbis.cs108.client.app;

import ch.unibas.dmi.dbis.cs108.Main;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.GameClient;
import ch.unibas.dmi.dbis.cs108.server.core.api.CommunicationAPI.PingFilter;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * ClientMain class
 * This class is the main entry point for the client application.
 */
public class ClientMain {
    private static final int MESSAGE_POLLING_DELAY = 50;
    private static final Logger logger = Logger.getLogger(ClientMain.class.getName());

    /**
     * Main method
     * Entry point for the client application.
     * @param args String[] Command line arguments
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GameClient client = null;
        AtomicBoolean running = new AtomicBoolean(true);
        logger.setFilter(new PingFilter());

        try {
            if (args.length < 2) {
                logger.warning("Missing server address. Usage: java ClientMain <serverip>:<serverport>");
                System.exit(1);
            }
            String[] serverAddress = args[1].split(":");
            if (serverAddress.length != 2) {
                logger.warning("Invalid server address. Expected: <serverip>:<serverport>");
                System.exit(1);
            }

            int serverport = Integer.parseInt(serverAddress[1]);

            String systemName = System.getProperty("user.name");
            Player localPlayer = new Player(systemName);

            logger.info("Connecting to server at " + serverAddress[0] + ":" + serverport + " as " + systemName + "...");

            client = new GameClient(serverAddress[0], serverport, localPlayer);

            if (checkClient(client)) return;

            Thread receiverThread = startMessageReceiverThread(client, running);

            logger.info("Connected. Type /help to view available commands");

            processInput(running, scanner, client);

            receiverThread.join(1000);

        } catch (Exception e) {
            logger.warning("Client start-up error: " + e.getMessage());
        } finally {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
            scanner.close();
            logger.info("Client terminated.");
        }
    }


    /**
     * Check if the client is connected to the server
     * @param client GameClient
     * @return boolean
     */
    private static boolean checkClient(GameClient client) {
        if (!client.isConnected()) { // Add this method to GameClient
            logger.warning("Client failed to connect to server. Exiting...");
            return true;
        }
        return false;
    }

    /**
     * Process user input
     * @param running AtomicBoolean
     * @param scanner Scanner
     * @param client GameClient
     */
    private static void processInput(AtomicBoolean running, Scanner scanner, GameClient client) {
        while (running.get()) {
            String input = scanner.nextLine().trim();

            if (input.equals("/exit")) {
                running.set(false);
                break;
            } else if (input.startsWith("/changeName ")) {
                String newName = input.replace("/changeName ", "").trim();
                client.changeName(newName);
            } else if (input.equals("/ping")) {
                client.sendPing();
            } else if (input.startsWith("/join ")) {
                String lobbyId = input.replace("/join ", "").trim();
                client.joinLobby(lobbyId);
            } else if (input.startsWith("/leave ")) {
                String lobbyId = input.replace("/leave ", "").trim();
                client.leaveLobby(lobbyId);
            } else if (input.startsWith("/create ")) {
                String lobbyName = input.replace("/create ", "").trim();
                client.createLobby(lobbyName);
            } else if (input.startsWith("/start ")) {
                String lobbyId = input.replace("/start ", "").trim();
                client.startGame();
            } else if (input.startsWith("/list")) {
                client.listLobbies();
            } else if (input.startsWith("/help")) {
                logger.info("Available commands: /changeName <name>, /ping, /exit, /join <lobbyId>, /leave <lobbyId>, /create <lobbyName>, /start <lobbyId>, /list, /help");
            } else {
                client.sendChat(input);
            }
        }
    }

    /**
     * Start a message receiver thread
     * @param client GameClient
     * @param running AtomicBoolean
     * @return Thread
     */
    private static Thread startMessageReceiverThread(GameClient client, AtomicBoolean running) {
        Thread thread = new Thread(() -> {
            try {
                while (running.get()) {
                    // This method needs to be implemented in GameClient
                    String message = client.receiveMessage();
                    if (message != null) {
                        System.out.println(message);
                    }
                    Thread.sleep(MESSAGE_POLLING_DELAY); // Small delay to prevent CPU hogging
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("Connection error: " + e.getMessage());
                running.set(false); // Signal main thread to exit
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }


}
