package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.SETTINGS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The GameServer class is responsible for managing the clients and their connections.
 * It listens for incoming connections and creates a new ClientHandler for each client.
 */
public class GameServer {
    private static final Logger logger = Logger.getLogger(GameServer.class.getName());

    private ScheduledExecutorService pingScheduler = Executors.newScheduledThreadPool(1);
    private boolean running;
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService executor;
    private List<ClientHandler> clients;
    private List<Lobby> lobbies;

    public GameServer(int port) {
        this.port = port;
        clients = new CopyOnWriteArrayList<>();
        executor = Executors.newCachedThreadPool();
        this.lobbies = new CopyOnWriteArrayList<>();
    }

    /**
     * Starts the server and listens for incoming connections.
     */
    public void start() {
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Server started on port " + port);

            // Schedule ping task to check if clients are still connected
            pingScheduler.scheduleAtFixedRate(this::pingClients, SETTINGS.Config.PING_INTERVAL.getValue(), SETTINGS.Config.PING_INTERVAL.getValue(), TimeUnit.MILLISECONDS);

            while (running) {
                try {
                    logger.info("Waiting for client connection...");
                    ClientHandler client = new ClientHandler(serverSocket.accept(), this);
                    clients.add(client);
                    executor.execute(client);
                } catch (SocketException se) {
                    if(!running) {
                        logger.info("Server socket closed. Exiting accept loop");
                        break; // Expected exception when server is shutting down
                    } else {
                        throw se;
                    }
                }
            }
        } catch (IOException e) {
            logger.severe("Server error:" + e.getMessage());
        }
    }

    /**
     * Stops the server and closes all connections.
     */
    public void shutdown() {
        running = false;
        pingScheduler.shutdown();
        // Disconnect all clients
        broadcast("STDN:");
        for (ClientHandler client : clients) {  //forcefully close all sockets and clear the clients list
            try {
                client.closeResources();
            } catch (Exception e) {
                logger.warning("Error closing client socket: " + e.getMessage());
            }
        }
        clients.clear();
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warning("Executor shutdown interrupted: " + e.getMessage());
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.warning("Error closing server socket: " + e.getMessage());
            }
        }
    }

    public void pingClients() {
        for (ClientHandler client : clients) {
            if (client.isRunning()) {
                client.sendPing();
            }
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     * @param message The message to broadcast
     */
    public void broadcast(String message) {
        for(ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * Removes a client from the list of connected clients.
     * @param client The client to remove
     */
    public void removeClient(ClientHandler client) {
        if (clients.contains(client)) {
            clients.remove(client);
            logger.info("Removed " + client);
        }
    }

    @Override
    public String toString() {
        return "GameServer{" +
                "port=" + port +
                ", running=" + running +
                ", clients=" + clients.size() +
                '}';
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public Lobby createLobby(String id, int maxPlayers) {
        Lobby lobby = new Lobby(id, maxPlayers);
        lobbies.add(lobby);
        logger.info("Created new Lobby :" + id);
        return lobby;
    }

    public Lobby getLobby(String id) {
        for(Lobby lobby : lobbies) {
            lobby.getId().equals(id) {
                return lobby;
            }
        }
        return null;
    }

    public void removeLobby(Lobby lobby) {
        lobbies.remove(lobby);
        logger.info("Removed Lobby :" + id);
    }

    public List<Lobby> getLobbies(){
        return lobbies;
    }

}
