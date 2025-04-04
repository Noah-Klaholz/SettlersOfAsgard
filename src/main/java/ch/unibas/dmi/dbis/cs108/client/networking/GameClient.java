package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.core.commands.chat.ChatCommand;
import ch.unibas.dmi.dbis.cs108.client.core.commands.chat.PongCommand;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.core.observer.GameEventListener;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.DisplayFormatter;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.ErrorMessageParser;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.MessageParser;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * GameClient class is responsible for handling the client-side of the game
 */
public class GameClient {
    private static final Logger logger = Logger.getLogger(GameClient.class.getName());
    private final SocketHandler socketHandler;
    private final CommandSender commandSender;
    private final MessageParser parser;
    private final ErrorMessageParser errorMessageParser;
    private final Player localPlayer;
    private final AtomicLong lastPingTime = new AtomicLong(0);
    private final List<GameEventListener> gameEventListeners = new ArrayList<>();
    private boolean connected = false;
    private ScheduledExecutorService pingScheduler = Executors.newScheduledThreadPool(1);

    /**
     * Constructor
     *
     * @param host        String
     * @param port        int
     * @param localPlayer Player
     * @throws IOException
     */
    public GameClient(String host, int port, Player localPlayer) throws IOException {
        this.localPlayer = localPlayer;
        try {
            this.socketHandler = new SocketHandler(host, port);
            this.errorMessageParser = new ErrorMessageParser();
            this.parser = new MessageParser();
            this.commandSender = new CommandSender(socketHandler);
            this.connected = true;
            logger.info("Connected to " + host + ":" + port);
            // Schedule ping task
            // Add timeout check
            pingScheduler.scheduleAtFixedRate(() -> {
                        // Check if we're waiting for a ping response
                        if (lastPingTime.get() > 0) {
                            long elapsed = Instant.now().toEpochMilli() - lastPingTime.get();
                            if (elapsed > SETTINGS.Config.TIMEOUT.getValue()) {
                                logger.severe("Ping timeout detected. Disconnecting...");
                                disconnect();
                                return;
                            }
                        }
                        sendPing();
                    }, SETTINGS.Config.PING_INTERVAL.getValue(),
                    SETTINGS.Config.PING_INTERVAL.getValue(), TimeUnit.MILLISECONDS);

            // Send initial connection message
            commandSender.sendRegister(localPlayer);
        } catch (IOException e) {
            this.connected = false;
            logger.severe("Failed to initialize GameClient: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Sends a chat message to the server
     *
     * @param message String
     */
    public void sendChat(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        if (isConnected()) {
            try {
                commandSender.sendChatCommand(new ChatCommand(localPlayer, message));
            } catch (Exception e) {
                logger.severe("Failed to send chat message: " + e.getMessage());
            }
        }
    }

    /**
     * Checks if the client is connected to the server
     *
     * @return boolean
     */
    public boolean isConnected() {
        return connected && socketHandler != null && socketHandler.isConnected();
    }

    /**
     * Disconnects the client from the server
     */
    public void disconnect() {
        // Always stop the ping scheduler first, regardless of connection state
        if (pingScheduler != null && !pingScheduler.isShutdown()) {
            pingScheduler.shutdownNow(); // Force immediate shutdown
            pingScheduler = Executors.newScheduledThreadPool(1); // Create a new instance for future reconnects
        }

        if (isConnected()) {
            // Send disconnect message before closing if possible
            try {
                commandSender.sendDisconnect(localPlayer);
            } catch (Exception e) {
                // Just log, continue with disconnect
                logger.info("Failed to send disconnect message: " + e.getMessage());
            }

            try {
                connected = false; // Set flag first to prevent recursive calls
                socketHandler.close();

                // Reset ping time
                lastPingTime.set(0);

                logger.info("Disconnected from server");
            } catch (Exception e) {
                logger.severe("Error during disconnect: " + e.getMessage());
            }
        }
    }

    /**
     * Changes the name of the local player
     *
     * @param newName String
     */
    public void changeName(String newName) {
        if (isConnected()) {
            try {
                commandSender.sendChangeName(newName);
            } catch (Exception e) {
                logger.severe("Failed to change name: " + e.getMessage());
            }
        }
    }

    /**
     * Getter for the local player
     *
     * @param newName String
     */
    public void setName(String newName) {
        localPlayer.setName(newName);
    }

    /**
     * Sends a ping message to the server
     */
    public void sendPing() {
        if (isConnected()) {
            try {
                lastPingTime.set(Instant.now().toEpochMilli());
                commandSender.sendPing(localPlayer);
            } catch (Exception e) {
                logger.severe("Failed to send ping: " + e.getMessage());
            }
        }
    }

    /**
     * Creates a new lobby
     *
     * @param lobbyName the name of the lobby
     */
    public void createLobby(String lobbyName) {
        if (isConnected()) {
            try {
                commandSender.sendCreateLobby(localPlayer, lobbyName);
            } catch (Exception e) {
                logger.severe("Failed to create lobby: " + e.getMessage());
            }
        }
    }

    /**
     * Joins a lobby
     *
     * @param lobbyName the name of the lobby
     */
    public void joinLobby(String lobbyName) {
        if (isConnected()) {
            try {
                commandSender.sendJoinLobby(localPlayer, lobbyName);
            } catch (Exception e) {
                logger.severe("Failed to join lobby: " + e.getMessage());
            }
        }
    }

    /**
     * Receives a message from the server
     *
     * @return String
     */
    public String receiveMessage() {
        if (!isConnected()) {
            return null;
        }
        try {
            String rawMessage = socketHandler.receive();
            if (rawMessage != null) {
                if (rawMessage.startsWith("STDN$")) {
                    System.out.println("Server sent shutdown Command, disconnecting and shutting down.");
                    disconnect();
                    return "Server has shut down. Client will terminate.";
                }
                // Automatically respond to server pings
                if (rawMessage.startsWith("PING$")) {
                    String serverId = rawMessage.split("\\$").length > 1 ? rawMessage.split("\\$")[1] : "server";
                    commandSender.sendPongCommand(new PongCommand(localPlayer, serverId));
                    return null;
                }
                // Handle pong responses
                else if (rawMessage.startsWith("OK$PING$")) {
                    // Only show result if we initiated a ping command
                    if (lastPingTime.get() > 0) {
                        long roundTripTime = Instant.now().toEpochMilli() - lastPingTime.get();
                        if (roundTripTime > SETTINGS.Config.TIMEOUT.getValue()) {
                            logger.severe("Server timed out, disconnecting.");
                            disconnect();
                        }
                        lastPingTime.set(0); // Reset ping time
                    }
                    return null;
                } else if (rawMessage.startsWith("OK$CHAN$")) {
                    String newName = rawMessage.replace("OK$CHAN$", "").trim();
                    localPlayer.setName(newName);
                } else if (rawMessage.startsWith("ERR$")) {
                    errorMessageParser.parseErrorMessage(rawMessage, this);
                    return DisplayFormatter.formatForDisplay(rawMessage);
                } else {
                    // Format the message for display
                    return DisplayFormatter.formatForDisplay(rawMessage);
                }
            }
        } catch (IOException e) {
            logger.severe("Connection lost: " + e.getMessage());
            disconnect();
            return "Connection lost: " + e.getMessage();
        }
        return null;
    }

    /**
     * Leaves the current lobby
     */
    public void leaveLobby() {
        if (isConnected()) {
            try {
                commandSender.sendLeaveLobby(localPlayer);
            } catch (Exception e) {
                logger.severe("Failed to leave lobby: " + e.getMessage());
            }
        }
    }

    /**
     * Starts the game in the current lobby
     */
    public void startGame() {
        if (isConnected()) {
            try {
                commandSender.sendStartGame();
            } catch (Exception e) {
                logger.severe("Failed to start game in lobby: " + e.getMessage());
            }
        }
    }

    /**
     * Lists all available lobbies
     */
    public void listLobbies() {
        if (isConnected()) {
            try {
                commandSender.sendListLobbies();
            } catch (Exception e) {
                logger.severe("Failed to list lobbies: " + e.getMessage());
            }
        }
    }

    /**
     * Lists all players in the current lobby
     */
    public void listLobbyPlayers() {
        if (isConnected()) {
            try {
                commandSender.sendListLobbyPlayers();
            } catch (Exception e) {
                logger.severe("Failed to list lobby players: " + e.getMessage());
            }
        }
    }

    /**
     * Lists all players in the server
     */
    public void listAllPlayers() {
        if (isConnected()) {
            try {
                commandSender.sendListAllPlayers();
            } catch (Exception e) {
                logger.severe("Failed to list all players: " + e.getMessage());
            }
        }
    }

    public void sendPrivateChat(String input) {
        if(isConnected()) {
            try {
                String message = input.replace("@", "").trim();
                commandSender.sendWhisper(localPlayer.getName(), message);
            } catch (Exception e) {
                logger.severe("Failed to send private message: " + e.getMessage());
            }
        }
    }

    public void sendLobbyChat(String input) {
        if (input.trim().isEmpty()) {
            return;
        }
        if (isConnected()) {
            try {
                commandSender.sendLobbyChatCommand(new ChatCommand(localPlayer, input));
            } catch (Exception e) {
                logger.severe("Failed to send chat message: " + e.getMessage());
            }
        }
    }

    public void registerGameEventListener(GameEventListener listener) {
        if (!gameEventListeners.contains(listener)) {
            gameEventListeners.add(listener);
            logger.info("Registered new game event listener");
        }
    }

    public void unregisterGameEventListener(GameEventListener listener) {
        gameEventListeners.remove(listener);
    }

    // Call this method when messages are received from server
    protected void notifyListeners(String message) {
        for (GameEventListener listener : new ArrayList<>(gameEventListeners)) {
            listener.onMessageReceived(message);
        }
    }
}
