package ch.unibas.dmi.dbis.cs108.server.networking;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.server.core.model.Leaderboard;
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
    /** Scheduler for periodic ping tasks to check client connection */
    private final ScheduledExecutorService pingScheduler;
    /** The port number on which the server listens for connections. */
    private final int port;
    /** Thread pool executor for handling client connections */
    private final ExecutorService executor;
    /** Thread-safe list of currently connected clients */
    private final List<ClientHandler> clients;
    /** Thread-safe list of active game lobbies */
    private final List<Lobby> lobbies;
    /** Leaderboard (global) */
    private final Leaderboard leaderboard;
    /** Flag indicating whether the server is currently running */
    private volatile boolean running;
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
        this.pingScheduler = Executors.newScheduledThreadPool(1);
        this.lobbies = new CopyOnWriteArrayList<>();
        this.leaderboard = new Leaderboard();
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
            pingScheduler.scheduleAtFixedRate(
                    this::checkClientConnections,
                    SETTINGS.Config.PING_INTERVAL.getValue(),
                    SETTINGS.Config.PING_INTERVAL.getValue(),
                    TimeUnit.MILLISECONDS
            );

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
        pingScheduler.shutdown();
        executor.shutdown();
        clients.forEach(ClientHandler::shutdown);
        clients.clear();
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            logger.warning("Error closing server socket: " + e.getMessage());
        }
    }

    /**
     * Checks if all clients are still connected and sends a ping to each.
     * If a client is not connected, it will be removed from the list of clients.
     */
    public void checkClientConnections() {
        clients.forEach(client -> {
            if (client.isConnected()) {
                client.sendPing();
            }
        });
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message The message to broadcast
     */
    public void broadcast(String message) {
        clients.stream()
                .filter(ClientHandler::isConnected)
                .forEach(client -> {
                    client.sendMessage(message);
                });
    }

    /**
     * Removes a client from the list of connected clients.
     *
     * @param client The client to remove
     */
    public void removeClient(ClientHandler client) {
        if (clients.remove(client)) {
            // Only remove from lobby if client is shutting down completely
            if (client.isShutdown()) {
                Lobby clientLobby = client.getCurrentLobby();
                if (clientLobby != null) {
                    clientLobby.removePlayer(client);
                    if (clientLobby.isEmpty()) {
                        removeLobby(clientLobby);
                    }
                }
            }
            logger.info("Removed client: " + client);
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
        Lobby lobby = new Lobby(id, maxPlayers, leaderboard);
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
        if (playerName == null) {
            return false;
        }
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
     * @param lobby The lobby to remove
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

    /**
     * Returns a string representation of all connected players.
     *
     * @return A string with the names of all connected players
     */
    public String listPlayers() {
        if (clients.isEmpty()) {
            return "No available players";
        }

        return clients.stream()
                .map(ClientHandler::getPlayerName)
                .collect(Collectors.joining(", "));

    }

    /**
     * Checks if the server is running.
     *
     * @return true if the server is running, false otherwise.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Gets the executor.
     *
     * @return the executor.
     */
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Gets the pingScheduler.
     *
     * @return the pingScheduler.
     */
    public ScheduledExecutorService getPingScheduler() {
        return pingScheduler;
    }

    /**
     * Gets the global leaderboard
     *
     * @return the leaderboard
     */
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    /**
     * Finds a client handler by player name, even if disconnected
     *
     * @param playerName The name of the player to find
     *
     *                   @return The ClientHandler object associated with the player name, or null if not found
     */
                        public ClientHandler findClientHandler(String playerName) {
        return clients.stream()
                .filter(c -> c.getPlayer() != null)
                .filter(c -> playerName.equals(c.getPlayerName()))
                .findFirst()
                .orElse(null);
    }
}
