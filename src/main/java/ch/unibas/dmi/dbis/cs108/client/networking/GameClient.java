package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.client.core.commands.ChatCommand;
import ch.unibas.dmi.dbis.cs108.client.core.commands.PingCommand;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.MessageParser;

import java.io.IOException;
import java.time.Instant;
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
                commandSender.sendDisconnect(localPlayer);
                socketHandler.close();
                connected = false;
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
                commandSender.sendChangeName(localPlayer, newName);
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
                        lastPingTime.set(0); // Reset ping time
                        return "Server responded with pong! Round-trip time: " + roundTripTime + "ms";
                    }
                    return null; // Don't show automatic pong responses
                } else if (rawMessage.startsWith("CHAT$")) {
                    return parser.parseChatMessage(rawMessage);
                } else if (rawMessage.startsWith("REGISTERED$")) {
                    return "Successfully registered with ID: " + parser.parseRegistrationResponse(rawMessage);
                } else {
                    return rawMessage;
                }
            }
        } catch (IOException e) {
            connected = false;
            return "Connection error: " + e.getMessage();
        }
        return null;
    }
}
