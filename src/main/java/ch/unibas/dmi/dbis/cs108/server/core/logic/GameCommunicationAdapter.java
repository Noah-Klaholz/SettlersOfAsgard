package ch.unibas.dmi.dbis.cs108.server.core.logic;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adapter class to connect GameLogic (using CommunicationAPI) with ClientHandler
 * Implements the Mediator pattern to handle communication between components
 */
public class GameCommunicationAdapter implements CommunicationAPI {
    private final List<ClientHandler> clients;
    private final GameLogic gameLogic;

    public GameCommunicationAdapter(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
        this.clients = new CopyOnWriteArrayList<>();
    }

    public void addClient(ClientHandler client) {
        clients.add(client);
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    @Override
    public void sendMessage(String message) {
        // Broadcast message to all connected clients
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    @Override
    public void processMessage(String received) {
        // Forward message to game logic for processing
        gameLogic.processMessage(received);
    }

    /**
     * Handle message from client
     */
    public void handleClientMessage(String message, ClientHandler client) {
        // Process the message and determine if it's a command
        processMessage(message);
    }
}