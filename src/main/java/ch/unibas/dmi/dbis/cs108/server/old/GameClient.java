package ch.unibas.dmi.dbis.cs108.server.old;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * The GameClient class is responsible for connecting to the server and sending/receiving messages.
 */
public class GameClient implements CommunicationAPI {
    private final Logger logger = Logger.getLogger(GameClient.class.getName());
    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private long lastPingTime = System.currentTimeMillis();
    private final ScheduledExecutorService pingScheduler = Executors.newScheduledThreadPool(1);

    /**
     * Contstructor Method for Gameclient
     *
     * @param host ServerIp Adress
     * @param port ServerPort
     */
    public GameClient(String host, int port) {
        logger.setFilter(new PingFilter());
        this.host = host;
        this.port = port;
    }

    /**
     * Connects to the server.
     * Creates new Thread for receiving messages
     * Creates new Thread for reading inputs from the terminal
     */
    public void connect() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logger.info("Connected to server on port " + port);

            // Schedule ping task
            pingScheduler.scheduleAtFixedRate(this::sendPing, SETTINGS.Config.PING_INTERVAL.getValue(), SETTINGS.Config.PING_INTERVAL.getValue(), TimeUnit.MILLISECONDS);

            new Thread(() -> {
                String received;
                try {
                    while ((received = in.readLine()) != null) {
                        processMessage(received);
                    }
                } catch (SocketException se) {
                    logger.info("Socket closed, exiting reading thread"); // Expected during shutdown
                } catch (IOException e) {
                    logger.warning("IO exception: " + e.getMessage());
                }
            }).start();

            // Start a new thread to read inputs from the terminal
            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String userInput = scanner.nextLine();
                    sendMessage(userInput);
                }
            }).start();
        } catch (Exception e) {
            logger.warning("Server exception: " + e.getMessage());
        }
    }

    /**
     * Disconnects the client from the server in following steps:
     * - Shuts down the ping scheduler
     * - Closes socket
     */
    public void disconnect() {
        pingScheduler.shutdown();
        try {
            socket.close();
        } catch (IOException e) {
            logger.warning("IO exception: " + e.getMessage());
        }
        System.out.println("Client Disconnected from port " + port);
    }

    /**
     * Starts the client and sends a test message to the server.
     */
    public void start() {
        sendMessage("TEST$arg1,arg2,arg3");    // test command
    }

    /**
     * Sends a Ping to the Server using the sendMessage Method (only works when connected)
     * Checks if the time since the last ping has exceeded the timeout value in
     *
     * @see SETTINGS
     */
    public void sendPing() {
        if (System.currentTimeMillis() - lastPingTime > SETTINGS.Config.TIMEOUT.getValue()) {
            logger.warning("Server timed out, disconnecting...");
            disconnect();
        } else {
            sendMessage("PING$");
        }
    }

    /**
     * Sends a message to the server.
     *
     * @param message The message to send
     */
    @Override
    public void sendMessage(String message) {
        out.println(message);
        logger.info("Sent message to server: " + message); // Log the sent message
    }

    /**
     * Handles a received chat message from the server.
     * For now: Just log the message
     *
     * @param cmd The command to handle
     */
    public void handleRecievedChatMessage(Command cmd) {
        logger.info("Received chat message: " + cmd);
    }

    /**
     * Receives a message from the server and processes it. Answers with an OK or ERR response depending on success of processing.
     *
     * @param received The received message from the server
     *                 Message String should be in the format "commandName:arg1,arg2,arg3"
     */
    @Override
    public void processMessage(String received) {
        Command cmd = new Command(received);
        if (cmd.isValid()) {
            logger.info("Client processing " + cmd);

            NetworkProtocol.Commands command;
            try {
                command = NetworkProtocol.Commands.fromCommand(cmd.getCommand());
            } catch (IllegalArgumentException e) {
                logger.warning("Unknown command: " + cmd.getCommand());
                return;
            }
            switch (command) {
                case CHATGLOBAL:
                    handleRecievedChatMessage(cmd);
                case PING:
                    lastPingTime = System.currentTimeMillis();
                    break;
                case TEST:
                    logger.info("TEST");
                    break;
                case SHUTDOWN:
                    logger.info("Server sent a shutdown command. Disconnecting...");
                    disconnect();
                    break;
                case OK:
                    break;
                case ERROR:
                    logger.info("Server sent an error command.");
                    break;
                default:
                    logger.warning("Unknown command: " + cmd.getCommand());
            }
        } else {
            logger.warning("Invalid command: " + cmd);
        }
    }
}
