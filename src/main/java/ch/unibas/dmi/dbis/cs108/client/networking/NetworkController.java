package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.core.NetworkClient;
import ch.unibas.dmi.dbis.cs108.client.networking.core.SocketNetworkClient;
import ch.unibas.dmi.dbis.cs108.client.networking.events.ConnectionEvent;
import ch.unibas.dmi.dbis.cs108.client.networking.events.EventDispatcher;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.ProtocolTranslator;

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
    private static final Logger LOGGER = Logger.getLogger(NetworkController.class.getName());

    private final NetworkClient networkClient;
    private final ProtocolTranslator translator;
    private final EventDispatcher eventDispatcher;
    private final Player localPlayer;
    private final AtomicLong lastPingTime = new AtomicLong(0);
    private ScheduledExecutorService pingScheduler;

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
                Logger.getGlobal().info("NetworkController: Disconnected from server: " + cause.getMessage());
                stopPingScheduler();
                eventDispatcher.dispatchEvent(new ConnectionEvent(
                        ConnectionEvent.ConnectionState.DISCONNECTED,
                        "Connection lost: " + cause.getMessage()));
            }
        });
    }

    /**
     * Connects to the game server.
     *
     * @param host The server host address.
     * @param port The server port number.
     */
    public void connect(String host, int port) {
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
     * Sends a disconnect message to the server and stops the ping scheduler.
     */
    public void disconnect() {
        if (networkClient.isConnected()) {
            String disconnectMessage = translator.formatDisconnect(localPlayer.getName());
            networkClient.send(disconnectMessage)
                    .exceptionally(ex -> {
                        LOGGER.warning("Error sending disconnect message: " + ex.getMessage());
                        return null;
                    })
                    .thenRun(() -> {
                        stopPingScheduler();
                        networkClient.disconnect();
                    });
        } else {
            networkClient.disconnect();
        }
        eventDispatcher.dispatchEvent(new ConnectionEvent(
                ConnectionEvent.ConnectionState.DISCONNECTED,
                "Lost connection to server."));
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
     *
     * @return true if connected and not closed, false otherwise.
     */
    private void startPingScheduler() {
        stopPingScheduler();
        pingScheduler = Executors.newSingleThreadScheduledExecutor();
        pingScheduler.scheduleAtFixedRate(() -> {
            if (lastPingTime.get() > 0) {
                long elapsed = Instant.now().toEpochMilli() - lastPingTime.get();
                if (elapsed > SETTINGS.Config.TIMEOUT.getValue()) {
                    LOGGER.severe("Ping timeout detected. Disconnecting...");
                    disconnect();
                    return;
                }
            }
            sendPing();
        }, SETTINGS.Config.PING_INTERVAL.getValue(),
                SETTINGS.Config.PING_INTERVAL.getValue(), TimeUnit.MILLISECONDS);
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
     * Handles incoming messages from the server.
     * This method processes different types of messages, including PING and OK$PING.
     *
     * @param message The incoming message from the server.
     */
    private void handleIncomingMessage(String message) {
        if (message.startsWith("PING$")) {
            // Auto-respond to ping with pong.
            String pongMessage = translator.formatPong(localPlayer.getName());
            networkClient.send(pongMessage);
            return;
        }
        if (message.startsWith("OK$PING$")) {
            if (lastPingTime.get() > 0) {
                long rtt = Instant.now().toEpochMilli() - lastPingTime.get();
                LOGGER.fine("Ping RTT: " + rtt + "ms");
                lastPingTime.set(0);
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
     * sends a ping message to the server.
     * This method is called periodically to check the connection status.
     * It updates the lastPingTime to the current time.
     * If the server does not respond within the timeout period,
     * the client will disconnect.
     */
    private void sendPing() {
        lastPingTime.set(Instant.now().toEpochMilli());
        String pingMessage = "PING$" + localPlayer.getName();
        networkClient.send(pingMessage)
                .exceptionally(ex -> {
                    LOGGER.warning("Error sending ping: " + ex.getMessage());
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
     * @param x          The x-coordinate to place the structure.
     * @param y          The y-coordinate to place the structure.
     * @param structureID The ID of the structure to place.
     */
    public void placeStructure(int x, int y, int structureID) {
        String message = translator.formatPlaceStructure(x, y, structureID);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to use a structure with the specified ID.
     *
     * @param x          The x-coordinate of the structure.
     * @param y          The y-coordinate of the structure.
     * @param structureID The ID of the structure to use.
     */
    public void useStructure(int x, int y, int structureID) {
        String message = translator.formatUseStructure(x, y, structureID);
        networkClient.send(message);
    }

    /**
     * Sends a message to the server to use an artifact with the specified ID.
     *
     * @param artifactID The ID of the artifact to use.
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
     * Sends a message to the server to get the prices of items.
     * The server will respond with a message containing the prices.
     */
    public void getPrices() {
        String message = translator.formatGetPrices();
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
    // Add getter for localPlayer if needed by other components (like
    // CommunicationMediator)
    public Player getLocalPlayer() {
        return localPlayer;
    }
}
