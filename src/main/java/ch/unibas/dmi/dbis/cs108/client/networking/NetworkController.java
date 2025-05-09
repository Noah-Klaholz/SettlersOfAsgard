package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.networking.core.NetworkClient;
import ch.unibas.dmi.dbis.cs108.client.networking.core.SocketNetworkClient;
import ch.unibas.dmi.dbis.cs108.client.networking.events.ConnectionEvent;
import ch.unibas.dmi.dbis.cs108.client.networking.events.EventDispatcher;
import ch.unibas.dmi.dbis.cs108.client.networking.events.ShutdownEvent;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.ProtocolTranslator;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import javafx.application.Platform;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * NetworkController is responsible for managing the network connection to the game server.
 * It handles sending and receiving messages, pinging the server, and managing the connection state.
 */
public class NetworkController {
    /** Logger to log logging */
    private static final Logger LOGGER = Logger.getLogger(NetworkController.class.getName());
    /** Network Client */
    private final NetworkClient networkClient;
    /** Protocol Translator */
    private final ProtocolTranslator translator;
    /** Event Dispatcher */
    private final EventDispatcher eventDispatcher;
    /** Player object */
    private final Player localPlayer;
    /** Last ping time */
    private final AtomicLong lastPingTime = new AtomicLong(0);
    /** Ping Scheduler */
    private ScheduledExecutorService pingScheduler;
    /** Flag to indicate if the client is currently reconnecting */
    private volatile boolean isReconnecting;
    /** Counter of the current reconnection attempt */
    private int reconnectAttempts;
    /** The ip of the server (saved for reconnection attempts) */
    String serverHost;
    /** The port of the connection (saved for reconnection attempts) */
    int serverPort;
    /** Reconnect Scheduler */
    private ScheduledExecutorService reconnectTimer;

    /**
     * Constructor for NetworkController.
     *
     * @param localPlayer The local player instance representing the current user.
     */
    public NetworkController(Player localPlayer) {
        this.localPlayer = localPlayer;
        this.networkClient = new SocketNetworkClient();
        this.eventDispatcher = EventDispatcher.getInstance();
        this.translator = new ProtocolTranslator(eventDispatcher);
        isReconnecting = false;
        reconnectAttempts = 0;
        reconnectTimer = Executors.newSingleThreadScheduledExecutor();
        setupMessageHandler();
    }

    /**
     * Sets up the message handler for incoming messages from the server.
     * This method is called during the initialization of the NetworkController.
     */
    private void setupMessageHandler() {
        networkClient.setMessageHandler(new NetworkClient.MessageHandler() {
            @Override
            public void onMessage(String message) {
                handleIncomingMessage(message);
            }

            @Override
            public void onDisconnect(Throwable cause) {
                Platform.runLater(() -> handleConnectionLost(cause));
            }
        });
    }

    /**
     * This method is called when the connection is lost.
     *
     * @param cause the cause of the connection loss.
     */
    private void handleConnectionLost(Throwable cause) {
        LOGGER.info("Connection lost: " + cause.getMessage());
        stopPingScheduler();
        if (!isReconnecting) {
            eventDispatcher.dispatchEvent(new ConnectionEvent(
                    ConnectionEvent.ConnectionState.DISCONNECTED,
                    "Connection lost: " + cause.getMessage()
            ));
            attemptReconnect();
        }
    }

    /**
     * Connects to the game server.
     *
     * @param host The server host address.
     * @param port The server port number.
     */
    public void connect(String host, int port) {
        // save host and port for reconnection attempts
        this.serverHost = host;
        this.serverPort = port;
        eventDispatcher.dispatchEvent(new ConnectionEvent(
                ConnectionEvent.ConnectionState.CONNECTING,
                "Connecting to " + host + ":" + port));
        networkClient.connect(host, port)
                .thenRun(() -> {
                    eventDispatcher.dispatchEvent(new ConnectionEvent(
                            ConnectionEvent.ConnectionState.CONNECTED,
                            "Connected to " + host + ":" + port));
                    startPingScheduler();
                    register();
                })
                .exceptionally(ex -> {
                    eventDispatcher.dispatchEvent(new ConnectionEvent(
                            ConnectionEvent.ConnectionState.DISCONNECTED,
                            "Failed to connect: " + ex.getMessage()));
                    return null;
                });
    }

    /**
     * Disconnects from the game server.
     * Sends an exit message to the server and stops the ping scheduler.
     */
    public void disconnect() {
        isReconnecting = false;
        reconnectAttempts = 0;
        stopReconnectTimer();
        stopPingScheduler();
        eventDispatcher.dispatchEvent(new ConnectionEvent(
                ConnectionEvent.ConnectionState.DISCONNECTED,
                "Lost connection to server."));
        if (networkClient.isConnected()) {
            String disconnectMessage = translator.formatExit(localPlayer.getName());
            networkClient.send(disconnectMessage)
                    .exceptionally(ex -> {
                        LOGGER.warning("Error sending exit message: " + ex.getMessage());
                        return null;
                    })
                    .thenRun(this::performDisconnect);
        } else {
            networkClient.disconnect();
        }
    }

    /**
     * This method is called when the client disconnects from the server.
     */
    private void performDisconnect() {
        if (networkClient.isConnected()) {
            networkClient.disconnect();
        }
        eventDispatcher.dispatchEvent(new ConnectionEvent(
                ConnectionEvent.ConnectionState.DISCONNECTED,
                "Disconnected by user"
        ));
        lastPingTime.set(0);
    }


    /**
     * Checks if the client is connected to the server.
     *
     * @return true if connected, false otherwise.
     */
    public boolean isConnected() {
        return networkClient.isConnected();
    }

    /**
     * Checks if the client is connected to the server and if the connection is not closed.
     */
    private void startPingScheduler() {
        stopPingScheduler();
        pingScheduler = Executors.newSingleThreadScheduledExecutor();
        pingScheduler.scheduleAtFixedRate(() -> {
                    try {
                        if (!networkClient.isConnected()) {
                            return;
                        }

                        long lastPing = lastPingTime.get();
                        if (lastPing > 0) {
                            long elapsed = Instant.now().toEpochMilli() - lastPing;
                            LOGGER.fine("Ping check - elapsed: " + elapsed);

                            if (elapsed > SETTINGS.Config.TIMEOUT.getValue()) {
                                LOGGER.warning("Ping timeout exceeded");
                                Platform.runLater(this::handlePingTimeout);
                                return;
                            }
                        }
                        sendPing();
                    } catch (Exception e) {
                        LOGGER.warning("Ping scheduler error: " + e.getMessage());
                    }
                }, SETTINGS.Config.PING_INTERVAL.getValue(),
                SETTINGS.Config.PING_INTERVAL.getValue(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * This method is called when the ping timeout is detected.
     */
    private void handlePingTimeout() {
        eventDispatcher.dispatchEvent(new ConnectionEvent(
                ConnectionEvent.ConnectionState.DISCONNECTED,
                "Connection lost: " + localPlayer.getName()
        ));
        attemptReconnect();
    }

    /**
     * Attempts to reconnect to the server when connection is lost.
     * After MAX_RECONNECT_ATTEMPTS, gives up and handles the permanent disconnection.
     */
    private void attemptReconnect() {
        Platform.runLater(() -> {
            synchronized (this) {
                if (isReconnecting || reconnectAttempts >= SETTINGS.Config.MAX_RECONNECT_ATTEMPTS.getValue()) {
                    return;
                }
                isReconnecting = true;
                reconnectAttempts++;
            }

            LOGGER.info("Starting reconnect attempt " + reconnectAttempts +
                    "/" + SETTINGS.Config.MAX_RECONNECT_ATTEMPTS.getValue());

            // Immediate cleanup if still connected
            if (networkClient.isConnected()) {
                networkClient.disconnect();
            }

            // Schedule the reconnect attempt
            if (reconnectTimer == null || reconnectTimer.isShutdown()) {
                reconnectTimer = Executors.newSingleThreadScheduledExecutor();
            }

            reconnectTimer.schedule(() -> {
                try {
                    if (serverHost == null || serverPort == 0) {
                        throw new IOException("No host or port specified");
                    }

                    networkClient.connect(serverHost, serverPort)
                            .thenRun(() -> {
                                Platform.runLater(this::handleReconnectSuccess);
                            })
                            .exceptionally(ex -> {
                                Platform.runLater(() -> handleReconnectFailure(ex));
                                return null;
                            });
                } catch (Exception e) {
                    Platform.runLater(() -> handleReconnectFailure(e));
                }
            }, SETTINGS.Config.RECONNECT_DELAYS_MS.getValue(), TimeUnit.MILLISECONDS);
        });
    }

    /**
     * This method is called when the reconnection attempt failed.
     *
     * @param ex the cause of the reconnection failure.
     */
    private void handleReconnectFailure(Throwable ex) {
       synchronized (this) {
           isReconnecting = false;
           eventDispatcher.dispatchEvent(new ShutdownEvent("Failed to reconnect to the server."));
           LOGGER.info("Reconnect failed (" + reconnectAttempts + "/" + SETTINGS.Config.MAX_RECONNECT_ATTEMPTS.getValue() + "): " + ex.getMessage());
        }
    }


    /**
     * This method is called when the reconnection attempt was successful.
     */
    private void handleReconnectSuccess() {
        synchronized (this) {
            isReconnecting = false;
            reconnectAttempts = 0;
        }
        lastPingTime.set(0);
        LOGGER.info("Reconnected successfully!");
        eventDispatcher.dispatchEvent(new ConnectionEvent(
                ConnectionEvent.ConnectionState.CONNECTED,
                "Reconnected to server"));
        startPingScheduler();
        stopReconnectTimer();
    }

    /**
     * Stops the ping scheduler if it is running.
     * This method is called when the connection is lost or when the client is disconnected.
     */
    private void stopPingScheduler() {
        if (pingScheduler != null && !pingScheduler.isShutdown()) {
            pingScheduler.shutdownNow();
            pingScheduler = null;
        }
    }

    /**
     * Stops the reconnection scheduler if it is running.
     * This method is called when the reconnection attempt was
     * either successful or failed.
     */
    private void stopReconnectTimer() {
        if (reconnectTimer != null && !reconnectTimer.isShutdown()) {
            reconnectTimer.shutdownNow();
            reconnectTimer = null;
        }
    }

    /**
     * Handles incoming messages from the server.
     * This method processes different types of messages, including PING and OK$PING.
     *
     * @param message The incoming message from the server.
     */
    private void handleIncomingMessage(String message) {
        if (message.startsWith("PING$")) {
            LOGGER.fine("Received ping request");
            networkClient.send(translator.formatPong(localPlayer.getName()));
            return;
        }

        if (message.startsWith("OK$PING$")) {
            long pingTime = lastPingTime.get();
            if (pingTime > 0) {
                long rtt = Instant.now().toEpochMilli() - pingTime;
                LOGGER.fine("Received pong after " + rtt + "ms");
                lastPingTime.compareAndSet(pingTime, 0); // Atomic reset
            }
            return;
        }
        translator.processIncomingMessage(message);
    }

    // High-level API methods

    /**
     * Registers the local player with the server.
     * This method sends a registration message to the server with the player's name.
     */
    public void register() {
        String message = translator.formatRegister(localPlayer.getName());
        networkClient.send(message);
    }

    /**
     * Sends a global chat message to the server.
     *
     * @param content The content of the chat message.
     */
    public void sendGlobalChat(String content) {
        if (content == null || content.trim().isEmpty())
            return;
        String message = translator.formatGlobalChatMessage(localPlayer.getName(), content);
        networkClient.send(message);
    }

    /**
     * Sends a lobby chat message to the server.
     *
     * @param content The content of the chat message.
     */
    public void sendLobbyChat(String content) {
        if (content == null || content.trim().isEmpty())
            return;
        String message = translator.formatLobbyChatMessage(localPlayer.getName(), content);
        networkClient.send(message);
    }

    /**
     * Sends a private chat message to a specific recipient.
     *
     * @param recipient The name of the recipient.
     * @param content   The content of the chat message.
     */
    public void sendPrivateChat(String recipient, String content) {
        if (content == null || content.trim().isEmpty())
            return;
        String message = translator.formatWhisper(localPlayer.getName(), recipient, content);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to create a new lobby.
     *
     * @param lobbyName The name of the lobby to create.
     */
    public void createLobby(String lobbyName, int maxPlayers) {
        String message = translator.formatCreateLobby(localPlayer.getName(), lobbyName, maxPlayers);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to join an existing lobby.
     *
     * @param lobbyName The name of the lobby to join.
     */
    public void joinLobby(String lobbyName) {
        String message = translator.formatJoinLobby(localPlayer.getName(), lobbyName);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to leave the current lobby.
     */
    public void leaveLobby() {
        String message = translator.formatLeaveLobby(localPlayer.getName());
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to start the game.
     */
    public void startGame() {
        String message = translator.formatStartGame();
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to list all available lobbies.
     */
    public void listLobbies() {
        String message = translator.formatListLobbies();
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to change the player's name.
     *
     * @param newName The new name for the player.
     */
    public void changeName(String newName) {
        String message = translator.formatChangeName(newName);
        networkClient.send(message);
    }

    /**
     * Sends a ping message to the server.
     * This method is called periodically to check the connection status.
     * It updates the lastPingTime to the current time.
     * If the server does not respond within the timeout period,
     * the client will disconnect.
     */
    private void sendPing() {
        if (!networkClient.isConnected()) {
            LOGGER.warning("Not connected - skipping ping");
            return;
        }

        long now = Instant.now().toEpochMilli();
        lastPingTime.set(now);
        LOGGER.fine("Sending ping at " + now);

        networkClient.send("PING$" + localPlayer.getName())
                .exceptionally(ex -> {
                    LOGGER.warning("Ping send failed: " + ex.getMessage());
                    Platform.runLater(() -> handleConnectionLost(ex));
                    return null;
                });
    }

    /**
     * Sends a message to the server to list all players in a specific lobby.
     *
     * @param lobbyName The name of the lobby to list players from.
     */
    public void listLobbyPlayers(String lobbyName) {
        String message = translator.formatListLobbyPlayers(lobbyName);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to list all players in the game.
     */
    public void listAllPlayers() {
        String message = translator.formatListAllPlayers();
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to end the current turn.
     */
    public void endTurn() {
        String message = translator.formatEndTurn();
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to buy a tile at the specified coordinates.
     */
    public void buyTile(int x, int y) {
        String message = translator.formatBuyTile(x, y);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to place a statue with the specified ID.
     *
     * @param statueID The ID of the statue to place.
     */
    public void placeStatue(int x, int y, int statueID) {
        String message = translator.formatPlaceStatue(x, y, statueID);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to upgrade a statue with the specified ID.
     *
     * @param x The x-coordinate of the statue.
     */
    public void upgradeStatue(int x, int y, int statueID) {
        String message = translator.formatUpgradeStatue(x, y, statueID);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to use a statue with the specified ID.
     *
     * @param statueID The ID of the statue to use.
     * @param params   Additional parameters for using the statue.
     */
    public void useStatue(int x, int y, int statueID, String params) {
        String message = translator.formatUseStatue(x, y, statueID, params);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to place a structure with the specified ID.
     *
     * @param x           The x-coordinate to place the structure.
     * @param y           The y-coordinate to place the structure.
     * @param structureID The ID of the structure to place.
     */
    public void placeStructure(int x, int y, int structureID) {
        String message = translator.formatPlaceStructure(x, y, structureID);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to use a structure with the specified ID.
     *
     * @param x           The x-coordinate of the structure.
     * @param y           The y-coordinate of the structure.
     * @param structureID The ID of the structure to use.
     */
    public void useStructure(int x, int y, int structureID) {
        String message = translator.formatUseStructure(x, y, structureID);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to use an artifact with the specified ID.
     *
     * @param artifactID    The ID of the artifact to use.
     * @param playerAimedAt The name of the player to aim at.
     */
    public void usePlayerArtifact(int artifactID, String playerAimedAt) {
        String message = translator.formatUsePlayerArtifact(artifactID, playerAimedAt);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to use a field artifact at the specified coordinates.
     *
     * @param x          The x-coordinate of the artifact.
     * @param y          The y-coordinate of the artifact.
     * @param artifactID The ID of the artifact to use.
     */
    public void useFieldArtifact(int x, int y, int artifactID) {
        String message = translator.formatUseFieldArtifact(x, y, artifactID);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to use a cheat code.
     *
     * @param cheatCode The cheat code to use.
     */
    public void useCheatCode(String cheatCode) {
        String message = translator.formatCheatCode(cheatCode);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to get the game state.
     * The server will respond with a message containing the current game state.
     */
    public void getGameState() {
        String message = translator.formatGetGameStatus();
        networkClient.send(message);
    }

    /**
     * Requests leaderboard data from the server.
     * The server will respond with a message containing player rankings and scores.
     */
    public void getLeaderboard() {
        String message = translator.formatGetLeaderboard();
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to get the local player's information.
     */
    public Player getLocalPlayer() {
        return localPlayer;
    }
}
