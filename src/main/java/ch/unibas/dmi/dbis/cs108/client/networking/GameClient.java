package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.client.core.commands.ChatCommand;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.MessageParser;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * GameClient class is responsible for handling the client-side of the game
 */
public class GameClient {
    private final SocketHandler socketHandler;
    private final CommandSender commandSender;
    private final MessageParser parser;
    private final Player localPlayer;
    private final AtomicLong lastPingTime = new AtomicLong(0);
    private boolean connected = false;

    /**
     * Constructor
     * @param host String
     * @param port int
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
            throw e;
        }
    }

    /**
     * Sends a chat message to the server
     * @param message String
     */
    public void sendChat(String message) {
        if (isConnected()) {
            commandSender.sendChatCommand(new ChatCommand(localPlayer, message));
        }
    }

    /**
     * Checks if the client is connected to the server
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
            commandSender.sendDisconnect(localPlayer);
            socketHandler.close();
            connected = false;
        }
    }

    /**
     * Changes the name of the local player
     * @param newName String
     */
    public void changeName(String newName) {
        if (isConnected()) {
            commandSender.sendChangeName(localPlayer, newName);
            localPlayer.setName(newName);
        }
    }

    /**
     * Sends a ping message to the server
     */
    public void sendPing() {
        if (isConnected()) {
            lastPingTime.set(Instant.now().toEpochMilli());
            commandSender.sendPing(localPlayer);
        }
    }

    /**
     * Receives a message from the server
     * @return String
     */
    public String receiveMessage() {
        if (!isConnected()) {
            return null;
        }
        try {
            String rawMessage = socketHandler.receive();
            if (rawMessage != null) {
                // Parse and handle different message types
                if (rawMessage.startsWith("PONG$")) {
                    long roundTripTime = Instant.now().toEpochMilli() - lastPingTime.get();
                    return "Server responded with pong! Round-trip time: " + roundTripTime + "ms";
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
