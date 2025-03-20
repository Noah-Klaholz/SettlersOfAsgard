package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.core.observer.GameEventListener;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.MessageParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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

    public void changeName(String newName){

    }

    public void sendPing(){

    }

    public String receiveMessage(){
        return null;
    }
}
