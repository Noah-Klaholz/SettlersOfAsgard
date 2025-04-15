package ch.unibas.dmi.dbis.cs108.server.networking;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.PingFilter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The GameServer class is responsible for managing the clients and their connections.
 * It listens for incoming connections and creates a new ClientHandler for each client.
 */
public class GameServer {
    /** Logger instance for server logging */
    private static final Logger logger = Logger.getLogger(GameServer.class.getName());
    /** Scheduler for periodic ping tasks to check client conncetion */
    private final ScheduledExecutorService pingScheduler = Executors.newScheduledThreadPool(1);
    /** The port number on which the server listens for connections. */
    private final int port;
    /** Thread pool executor for handling client connections */
    private final ExecutorService executor;
    /** Thread-safe list of currently connected clients */
    private final List<ClientHandler> clients;
    /** Thread-safe list of active game lobbies */
    private final List<Lobby> lobbies;
    /** Flag indicating whether the server is currently running */
    private boolean running;
    /** The server socket used to accept client connections */
    private ServerSocket serverSocket;

    /**
     * Constructs a new GameServer instance that will listen on the specified port.
     *
     * @param port The port number to listen on
     */
    public GameServer(int port) {
        logger.setFilter(new PingFilter());
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
                    if (!running) {
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
        broadcast("STDN$");
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

    /**
     * Sends a ping message to all connected running clients.
     */
    public void pingClients() {
        for (ClientHandler client : clients) {
            if (client.isRunning()) {
                client.sendPing();
            } else {
                logger.info("Client " + client + " is not running. Removing from list.");
                removeClient(client);
            }
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message The message to broadcast
     */
    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * Removes a client from the list of connected clients.
     *
     * @param client The client to remove
     */
    public void removeClient(ClientHandler client) {
        if (clients.contains(client)) {
            clients.remove(client);
            Lobby clientLobby = client.getCurrentLobby();
            if (clientLobby != null) {
                clientLobby.removePlayer(client);
                if (clientLobby.isEmpty()) {
                    removeLobby(clientLobby);
                }
            }
            logger.info("Removed " + client);
        }
    }

    /**
     * Returns the port, the serverStatus (running or not) and the number of connected clients.
     *
     * @return A string representation of the server
     */
    @Override
    public String toString() {
        return "GameServer{" +
                "port=" + port +
                ", running=" + running +
                ", clients=" + clients.size() +
                '}';
    }

    /**
     * Returns a list of all connected clients.
     *
     * @return A list of connected clients as ClientHandler objects
     * @see ClientHandler
     */
    public List<ClientHandler> getClients() {
        return clients;
    }

    /**
     * Creates a new lobby with the given id and maximum number of players.
     *
     * @param id         The id of the lobby
     * @param maxPlayers The maximum number of players in the lobby
     * @return The created lobby
     * @see Lobby
     */
    public Lobby createLobby(String id, int maxPlayers) {
        if (getLobby(id) != null) { // If lobby with id already exists
            logger.warning("Lobby with id " + id + " already exists");
            return null;
        }
        Lobby lobby = new Lobby(id, maxPlayers);
        lobbies.add(lobby);
        logger.info("Created new Lobby: " + id);
        return lobby;
    }

    /**
     * Returns the lobby with the given id.
     *
     * @param id The string id of the lobby
     * @return The lobby with the given id or null if no lobby with that id exists
     * @see Lobby
     */
    public Lobby getLobby(String id) {
        for (Lobby lobby : lobbies) {
            if (lobby.getId().equals(id)) {
                return lobby;
            }
        }
        return null;
    }

    /**
     * Checks if a player with the given name is currently connected to the server.
     *
     * @param playerName The name to check for
     * @return true if a player with this name exists, false otherwise
     */
    public boolean containsPlayerName(String playerName) {
        for (ClientHandler client : clients) {
            if (client.getPlayer() != null) {
                if (client.getPlayerName().equals(playerName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes the given lobby from the list of lobbies.
     *
     * @see Lobby
     */
    public void removeLobby(Lobby lobby) {
        lobbies.remove(lobby);
        logger.info("Removed Lobby :" + lobby.getId());
    }

    /**
     * Returns a list of all lobbies.
     *
     * @return A list of all lobbies as Lobby objects
     * @see Lobby
     */
    public List<Lobby> getLobbies() {
        return lobbies;
    }

    public String listPlayers() {
        if (clients.isEmpty()) {
            return "No available players";
        }

        return clients.stream()
                .map(ClientHandler::getPlayerName)
                .collect(Collectors.joining(", "));

    }
}
