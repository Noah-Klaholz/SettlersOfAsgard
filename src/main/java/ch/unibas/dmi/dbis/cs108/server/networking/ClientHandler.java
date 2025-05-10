package ch.unibas.dmi.dbis.cs108.server.networking;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.networking.events.ConnectionEvent;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.core.structures.protocol.CommandHandler;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;
import ch.unibas.dmi.dbis.cs108.shared.protocol.ErrorsAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * The ClientHandler class is responsible for handling the communication between the server and a client.
 * It serves as an interface for the server to access the client's socket and send messages to the client.
 * It implements the Runnable interface to allow for multi-threading.
 * It also implements the CommunicationAPI interface to allow for communication with the server.
 */
public class ClientHandler implements Runnable, CommunicationAPI {
    /** Logger to log logging */
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    /** Connection state connected */
    private static final int STATE_CONNECTED = 0;
    /** Connection state disconnected */
    private static final int STATE_DISCONNECTED = 1;
    /** Connection state shutdown */
    private static final int STATE_SHUTDOWN = 2;
    /** Reference to a CommandHandler */
    private final CommandHandler ch;
    /** reference to the server */
    protected GameServer server;
    /** Reference to the current lobby of the client */
    protected Lobby currentLobby = null;
    /** Reference to the local player of the client */
    protected Player localPlayer = null;
    /** The socket for the client connection */
    private Socket socket;
    /** PrintWriter for sending messages to the client */
    private PrintWriter out;
    /** BufferedReader for receiving messages from the client */
    private BufferedReader in;
    /** Last time a ping was sent */
    private long lastPingTime = System.currentTimeMillis();
    /** Timeout scheduler */
    private final ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor();
    /** 0 if the player is connected, otherwise the currentMillis of the last disconnection */
    public long lastDisconnectionTime;
    private volatile int connectionState = STATE_CONNECTED;

    /**
     * Constructor for the ClientHandler class.
     *
     * @param socket the client's socket
     * @param server the GameServer
     */
    public ClientHandler(Socket socket, GameServer server) {
        logger.setFilter(new PingFilter());
        this.socket = socket;
        this.server = server;
        this.ch = new CommandHandler(this);
        try {
            socket.setSoTimeout(SETTINGS.Config.TIMEOUT.getValue());
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            logger.severe("Error setting up client handler: " + e.getMessage());
            closeResources();
            connectionState = STATE_SHUTDOWN;
        }
    }

    /**
     * The run method is called when the thread is started.
     * It listens for messages from the client and processes them.
     * If the client disconnects, it closes the resources and notifies the server to remove this client.
     */
    @Override
    public void run() {
        try {
            while (connectionState != STATE_SHUTDOWN) {
                String received;
                try {
                    received = in.readLine();
                    if (received == null) {
                        disconnect();
                        break;
                    }

                    if (connectionState == STATE_DISCONNECTED) {
                        reconnect();
                    }

                    processMessage(received);
                    lastPingTime = System.currentTimeMillis();

                } catch (IOException e) {
                    if (connectionState != STATE_SHUTDOWN) {
                        disconnect();
                    }
                    break;
                }
            }
        } finally {
            shutdown();
        }
    }

    /**
     * Sends a START command to the client to initiate game start
     */
    public void startGame() {
        sendMessage(NetworkProtocol.Commands.START + "$");
    }

    public synchronized void shutdown() {
        if (connectionState != STATE_SHUTDOWN) {
            connectionState = STATE_SHUTDOWN;
            timeoutScheduler.shutdownNow();
            closeResources();
        }
    }

    /**
     * Transition to connected state
     */
    public synchronized void reconnect() {
        connectionState = STATE_CONNECTED;
        if (currentLobby != null) {
            currentLobby.broadcastMessage("RECO$" + getPlayerName());
        }

        logger.info("Player " + localPlayer.getName() + " has reconnected.");
    }

    /*
    private void checkReconnectionTimeout() {
        if (connectionState == STATE_DISCONNECTED) {
            if (currentLobby != null) {
                currentLobby.endGame();
                currentLobby.removePlayer(this);
            }
            server.removeClient(this);
            shutdown();
        }
    }*/

    /**
     * Transition to disconnected state
     */
    public synchronized void disconnect() {
        if (connectionState == STATE_CONNECTED) {
            connectionState = STATE_DISCONNECTED;
            lastDisconnectionTime = System.currentTimeMillis();

            // Notify lobby
            if (currentLobby != null) {
                currentLobby.broadcastMessage("DISC$" + getPlayerName());
                if (currentLobby.getStatus().equals(Lobby.LobbyStatus.IN_GAME.getStatus())) {
                    currentLobby.endGame(); // current implementation: game ends immediately, no reconnect possible
                }
            }
            /*
            timeoutScheduler.schedule(
                    this::checkReconnectionTimeout,
                    SETTINGS.Config.GRACE_PERIOD.getValue(),
                    TimeUnit.MILLISECONDS
            );*/

            logger.info("Player " + localPlayer.getName() + " has disconnected.");
        }
    }

    /**
     * Closes the resources associated with the client handler.
     */
    private void closeResources() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            logger.warning("Error closing resources: " + e.getMessage());
        }
    }

    /**
     * Sends a String message to the client.
     *
     * @param message the message String to send
     */
    @Override
    public void sendMessage(String message) {
        if (connectionState == STATE_SHUTDOWN) return;

        try {
            if (out != null && !out.checkError()) {
                out.println(message);
                return;
            }
        } catch (Exception e) {
            logger.fine("Error sending message: " + e.getMessage());
        }
        disconnect();
    }

    /**
     * Sends a global chat message to all players in the server.
     *
     * @param message the message to send
     */
    public void sendGlobalChatMessage(String message) {
        server.broadcast(message);
    }

    /**
     * Sends a PING message to the corresponding client.
     * If the client does not respond within the timeout period, the client is disconnected.
     */
    public void sendPing() {
        if (out == null || out.checkError()) {
            disconnect();
            return;
        }
        if (System.currentTimeMillis() - lastPingTime > SETTINGS.Config.TIMEOUT.getValue()) {
            disconnect();
        } else {
            sendMessage("PING$");
        }
    }

    /**
     * Returns the server instance this client handler is connected to
     *
     * @return the GameServer instance
     */
    public GameServer getServer() {
        return server;
    }

    /**
     * Returns the current lobby the client is in.
     *
     * @return the current Lobby as a Lobby object
     * @see Lobby
     */
    public Lobby getCurrentLobby() {
        return currentLobby;
    }

    /**
     * Sets the current lobby the client is in.
     *
     * @param currentLobby the current lobby of the respective player
     * @see Lobby
     */
    public void setCurrentLobby(Lobby currentLobby) {
        this.currentLobby = currentLobby;
    }

    /**
     * This method returns the localPlayer variable assigned to the ClientHandler
     *
     * @return the current localPlayer state
     */
    public Player getPlayer() {
        return localPlayer;
    }

    /**
     * Sets the local player to the given Player argument
     *
     * @param player the player to be assigned to the ClientHandler
     */
    public void setPlayer(Player player) {
        localPlayer = player;
    }

    /**
     * This method returns the name of the localPlayer assigned to the ClientHandler
     *
     * @return the name of the current localPlayer
     */
    public String getPlayerName() {
        return localPlayer == null ? null : localPlayer.getName();
    }

    /**
     * Processes a received message from the client.
     *
     * @param received the received message String
     *                 The message is parsed into a Command object and processed accordingly.
     *                 If the command is invalid, an error message is sent to the client.
     *                 If the command is valid, it is processed according to the protocol.
     */
    @Override
    public void processMessage(String received) {
        // Update last ping time for any valid message
        lastPingTime = System.currentTimeMillis();

        if (received == null || received.trim().isEmpty()) {
            logger.warning("Received null or empty message");
            sendMessage("ERR$103$Null");
            return;
        }
        Command cmd = new Command(received, localPlayer);
        logger.info(cmd + " from message: " + received);
        if (cmd.isValid()) {
            if (cmd.isAdministrative()) {
                processAdminCommand(cmd);
            } else {
                if (currentLobby != null) {
                    if (Objects.equals(currentLobby.getStatus(), Lobby.LobbyStatus.IN_GAME.getStatus())) {
                        ch.getGameLogic().processCommand(cmd);
                    } else {
                        sendMessage("ERR$" + ErrorsAPI.Errors.NOT_IN_GAME.getError());
                    }
                } else {
                    sendMessage("ERR$" + ErrorsAPI.Errors.NOT_IN_LOBBY.getError());
                }
            }
        } else {
            logger.warning("ClientHandler: Invalid command: " + cmd);
        }
    }

    /**
     * Handles all administrative commands (not game-related).
     *
     * @param cmd The command received from the client.
     */
    private void processAdminCommand(Command cmd) {
        logger.info("Server processing " + cmd);

        NetworkProtocol.Commands command;
        try {
            command = NetworkProtocol.Commands.fromCommand(cmd.getCommand());
        } catch (IllegalArgumentException e) {
            logger.warning("Protocol-Unknown command: " + cmd.getCommand());
            return;
        }

        boolean answer = true; // Assume command has to be answered
        boolean worked = true; // Assume command was processed successfully
        switch (command) {
            case CHATLOBBY:
                answer = false;
                worked = ch.handleLobbyMessage(cmd);
                break;
            case CHATPRIVATE:
                answer = false;
                worked = ch.handlePrivateMessage(cmd);
                break;
            case CHATGLOBAL:
                answer = false;
                worked = ch.handleGlobalChatMessage(cmd);
                break;
            case PING:
                break;
            case LEADERBOARD:
                answer = false;
                worked = ch.handleGetLeaderboard();
                break;
            case TEST:
                answer = false;
                logger.info("TEST");
                break;
            case OK:
                answer = false;
                break;
            case ERROR:
                answer = false;
                logger.info("Client sent an error command.");
                break;
            case CREATELOBBY:
                worked = ch.handleCreateLobby(cmd);
                break;
            case JOIN:
                answer = false;
                worked = ch.handleJoinLobby(cmd);
                break;
            case LEAVE:
                answer = false;
                worked = ch.handleLeaveLobby();
                break;
            case START:
                answer = false;
                worked = ch.handleStartGame();
                break;
            case CHANGENAME:
                answer = false;
                worked = ch.handleChangeName(cmd);
                break;
            case REGISTER:
                answer = false;
                worked = ch.handleRegister(cmd);
                break;
            case LISTLOBBIES:
                answer = false;
                worked = ch.handleListLobbies();
                break;
            case LISTPLAYERS:
                answer = false;
                worked = ch.handleListPlayers(cmd);
                break;
            case RECONNECT:
                worked = ch.handleReconnect(cmd);
                break;
            case EXIT:
                worked = ch.handleExit();
                answer = false;
                break;
        }
        if (answer && worked) {
            sendMessage("OK$" + cmd); // Echo the command back to the client with an OK response
        }
    }

    public boolean isConnected() {
        return connectionState == STATE_CONNECTED;
    }

    public boolean isDisconnected() {
        return connectionState == STATE_DISCONNECTED;
    }

    public boolean isShutdown() {
        return connectionState == STATE_SHUTDOWN;
    }

}