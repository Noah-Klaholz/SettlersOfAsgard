package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.core.commands.ChatCommand;
import ch.unibas.dmi.dbis.cs108.client.core.commands.PingCommand;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.MessageParser;

import java.io.IOException;
import java.time.Instant;
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
    private final Player localPlayer;
    private final AtomicLong lastPingTime = new AtomicLong(0);
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
            this.parser = new MessageParser();
            this.commandSender = new CommandSender(socketHandler);
            this.connected = true;
            logger.info("Connected to " + host + ":" + port);
            // Schedule ping task
            pingScheduler.scheduleAtFixedRate(this::sendPing, SETTINGS.Config.PING_INTERVAL.getValue(), SETTINGS.Config.PING_INTERVAL.getValue(), TimeUnit.MILLISECONDS);

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
        if (isConnected()) {
            try {
                socketHandler.close();
                connected = false;
                if (pingScheduler != null) {
                    pingScheduler.shutdown();
                    try {
                        if (!pingScheduler.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                            pingScheduler.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        pingScheduler.shutdownNow();
                    }
                }
            } catch (Exception e) {
                logger.severe("Failed to disconnect: " + e.getMessage());
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
                localPlayer.setName(newName);
            } catch (Exception e) {
                logger.severe("Failed to change name: " + e.getMessage());
            }
        }
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
                if(rawMessage.startsWith("STDN$")) {
                    System.out.println("Server sent shutdown Command, disconnecting and shutting down.");
                    disconnect();
                    //TODO falls es noch mehr Ressourcen zum closen gibt, dann hier!
                }
                // Automatically respond to server pings
                if (rawMessage.startsWith("PING$")) {
                    // Extract server ID if present
                    String serverId = rawMessage.split("\\$").length > 1 ? rawMessage.split("\\$")[1] : "server";

                    commandSender.sendPingCommand(new PingCommand(localPlayer));
                    return null; // Don't show ping to user
                }
                // Handle pong responses
                else if (rawMessage.startsWith("OK$PING$")) {
                    // Only show result if we initiated a ping command
                    if (lastPingTime.get() > 0) {
                        long roundTripTime = Instant.now().toEpochMilli() - lastPingTime.get();
                        if(roundTripTime > SETTINGS.Config.TIMEOUT.getValue()) {
                            logger.severe("Server timed out, disconnecting.");
                            disconnect();
                        }
                        lastPingTime.set(0); // Reset ping time
                        return "Server answered PING$: Round-trip time: " + roundTripTime + "ms";
                    }
                    return null; // Don't show automatic pong responses
                } else if (rawMessage.startsWith("ERR$")) {
                    return "Error: " + parser.parseErrorResponse(rawMessage);
                }
                else {
                    return rawMessage;
                }
            }
        } catch (IOException e) {
            connected = false;
            return "Connection error: " + e.getMessage();
        }
        return null;
    }

    /**
     * Leaves the current lobby
     */
    public void leaveLobby(String lobbyName) {
        if (isConnected()) {
            try {
                commandSender.sendLeaveLobby(localPlayer, lobbyName);
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
}
