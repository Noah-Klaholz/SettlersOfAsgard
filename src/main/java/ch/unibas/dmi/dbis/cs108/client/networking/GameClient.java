package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.client.core.commands.ChatCommand;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.MessageParser;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class GameClient {
    private final SocketHandler socketHandler;
    private final CommandSender commandSender;
    private final MessageParser parser;
    private final Player localPlayer;
    private final AtomicLong lastPingTime = new AtomicLong(0);
    private boolean connected = false;


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


    public void sendChat(String message) {
        if (isConnected()) {
            commandSender.sendChatCommand(new ChatCommand(localPlayer, message));
        }
    }

    public boolean isConnected() {
        return connected && socketHandler != null && socketHandler.isConnected();
    }

    public void disconnect() {
        if (isConnected()) {
            commandSender.sendDisconnect(localPlayer);
            socketHandler.close();
            connected = false;
        }
    }

    public void changeName(String newName) {
        if (isConnected()) {
            commandSender.sendChangeName(localPlayer, newName);
            localPlayer.setName(newName);
        }
    }

    public void sendPing() {
        if (isConnected()) {
            lastPingTime.set(Instant.now().toEpochMilli());
            commandSender.sendPing(localPlayer);
        }
    }

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
