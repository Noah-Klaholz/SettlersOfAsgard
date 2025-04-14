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

public class NetworkController {
    private static final Logger LOGGER = Logger.getLogger(NetworkController.class.getName());

    private final NetworkClient networkClient;
    private final ProtocolTranslator translator;
    private final EventDispatcher eventDispatcher;
    private final Player localPlayer;
    private final AtomicLong lastPingTime = new AtomicLong(0);
    private ScheduledExecutorService pingScheduler;

    public NetworkController(Player localPlayer) {
        this.localPlayer = localPlayer;
        this.networkClient = new SocketNetworkClient();
        this.eventDispatcher = EventDispatcher.getInstance();
        this.translator = new ProtocolTranslator(eventDispatcher);
        setupMessageHandler();
    }

    private void setupMessageHandler() {
        networkClient.setMessageHandler(new NetworkClient.MessageHandler() {
            @Override
            public void onMessage(String message) {
                handleIncomingMessage(message);
            }

            @Override
            public void onDisconnect(Throwable cause) {
                stopPingScheduler();
                eventDispatcher.dispatchEvent(new ConnectionEvent(
                        ConnectionEvent.ConnectionState.DISCONNECTED,
                        "Connection lost: " + cause.getMessage()
                ));
            }
        });
    }

    public void connect(String host, int port) {
        eventDispatcher.dispatchEvent(new ConnectionEvent(
                ConnectionEvent.ConnectionState.CONNECTING,
                "Connecting to " + host + ":" + port
        ));
        networkClient.connect(host, port)
                .thenRun(() -> {
                    eventDispatcher.dispatchEvent(new ConnectionEvent(
                            ConnectionEvent.ConnectionState.CONNECTED,
                            "Connected to " + host + ":" + port
                    ));
                    startPingScheduler();
                    register();
                })
                .exceptionally(ex -> {
                    eventDispatcher.dispatchEvent(new ConnectionEvent(
                            ConnectionEvent.ConnectionState.DISCONNECTED,
                            "Failed to connect: " + ex.getMessage()
                    ));
                    return null;
                });
    }

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

    public boolean isConnected() {
        return networkClient.isConnected();
    }

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

    private void stopPingScheduler() {
        if (pingScheduler != null && !pingScheduler.isShutdown()) {
            pingScheduler.shutdownNow();
            pingScheduler = null;
        }
    }

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

    public void register() {
        String message = translator.formatRegister(localPlayer.getName());
        networkClient.send(message);
    }

    public void sendGlobalChat(String content) {
        if (content == null || content.trim().isEmpty()) return;
        String message = translator.formatGlobalChatMessage(localPlayer.getName(), content);
        networkClient.send(message);
    }

    public void sendLobbyChat(String content) {
        if (content == null || content.trim().isEmpty()) return;
        String message = translator.formatLobbyChatMessage(localPlayer.getName(), content);
        networkClient.send(message);
    }

    public void sendPrivateChat(String recipient, String content) {
        if (content == null || content.trim().isEmpty()) return;
        String message = translator.formatWhisper(localPlayer.getName(), recipient, content);
        networkClient.send(message);
    }

    public void createLobby(String lobbyName) {
        String message = translator.formatCreateLobby(localPlayer.getName(), lobbyName);
        networkClient.send(message);
    }

    public void joinLobby(String lobbyName) {
        String message = translator.formatJoinLobby(localPlayer.getName(), lobbyName);
        networkClient.send(message);
    }

    public void leaveLobby() {
        String message = translator.formatLeaveLobby(localPlayer.getName());
        networkClient.send(message);
    }

    public void startGame() {
        String message = translator.formatStartGame();
        networkClient.send(message);
    }

    public void listLobbies() {
        String message = translator.formatListLobbies();
        networkClient.send(message);
    }

    public void changeName(String newName) {
        String message = translator.formatChangeName(newName);
        networkClient.send(message);
    }

    private void sendPing() {
        lastPingTime.set(Instant.now().toEpochMilli());
        String pingMessage = "PING$" + localPlayer.getName();
        networkClient.send(pingMessage)
                .exceptionally(ex -> {
                    LOGGER.warning("Error sending ping: " + ex.getMessage());
                    return null;
                });
    }

    public void listLobbyPlayers(String lobbyName) {
        String message = translator.formatListLobbyPlayers(lobbyName);
        networkClient.send(message);
    }

    public void listAllPlayers() {
        String message = translator.formatListAllPlayers();
        networkClient.send(message);
    }

    public void endTurn() {
        String message = translator.formatEndTurn();
        networkClient.send(message);
    }

    public void buyTile(int x, int y) {
        String message = translator.formatBuyTile(x, y);
        networkClient.send(message);
    }

    public void buyStatue(int statueID) {
        String message = translator.formatBuyStatue(statueID);
        networkClient.send(message);
    }

    public void upgradeStatue(int x, int y, int statueID) {
        String message = translator.formatUpgradeStatue(x, y, statueID);
        networkClient.send(message);
    }

    public void useStatue(int x, int y, int statueID, String useType) {
        String message = translator.formatUseStatue(x, y, statueID, useType);
        networkClient.send(message);
    }

    public void placeStructure(int x, int y, int structureID) {
        String message = translator.formatPlaceStructure(x, y, structureID);
        networkClient.send(message);
    }

    public void useStructure(int x, int y, int structureID, String useType) {
        String message = translator.formatUseStructure(x, y, structureID, useType);
        networkClient.send(message);
    }

    public void usePlayerArtifact(int artifactID, String useType, String playerAimedAt) {
        String message = translator.formatUsePlayerArtifact(artifactID, useType, playerAimedAt);
        networkClient.send(message);
    }

    public void useFieldArtifact(int x, int y, int artifactID, String useType) {
        String message = translator.formatUseFieldArtifact(x, y, artifactID, useType);
        networkClient.send(message);
    }

    public void getPrices() {
        String message = translator.formatGetPrices();
        networkClient.send(message);
    }

    public void getGameState() {
        String message = translator.formatGetGameStatus();
        networkClient.send(message);
    }
}