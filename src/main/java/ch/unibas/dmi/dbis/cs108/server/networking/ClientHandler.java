package ch.unibas.dmi.dbis.cs108.server.networking;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

/**
 * The ClientHandler class is responsible for handling the communication between the server and a client.
 * It serves as an interface for the server to access the client's socket and send messages to the client.
 * It implements the Runnable interface to allow for multi-threading.
 * It also implements the CommunicationAPI interface to allow for communication with the server.
 */
public class ClientHandler implements Runnable, CommunicationAPI {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private long lastPingTime = System.currentTimeMillis();
    private GameServer server; // Reference to the GameServer
    private boolean running;
    private Lobby currentLobby;
    private Player localPlayer = null;
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());


    /**
     * Constructor for the ClientHandler class.
     * @param socket the client's socket
     * @param server the GameServer
     */
    public ClientHandler(Socket socket, GameServer server) {
        logger.setFilter(new PingFilter());
        this.socket = socket;
        this.server = server;
        this.running = true;
        try {
            socket.setSoTimeout(SETTINGS.Config.TIMEOUT.getValue()); // 5 second timeout
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            logger.severe("Error setting up client handler: " + e.getMessage());
            closeResources();
            running = false;
        }
    }

    /**
     * The run method is called when the thread is started.
     * It listens for messages from the client and processes them.
     * If the client disconnects, it closes the resources and notifies the server to remove this client.
     */
    @Override
    public void run() {
        String received;
        try {
            while (running && (received = in.readLine()) != null) {
                processMessage(received);
            }
        } catch (IOException e) {
            logger.info("Client disconnected unexpectedly: " + e.getMessage());
        } finally {
            closeResources();
            server.removeClient(this); // Notify the server to remove this client
            running = false;
        }
    }

    /**
     * Sends a String message to the client.
     * @param message the message String to send
     */
    @Override
    public void sendMessage(String message) {
        if (out != null && socket != null && !socket.isClosed()) {
            out.println(message);
            if (out.checkError()) { // Check if there was an error during write
                logger.warning("Error sending message to client, closing connection.");
                closeResources();
                server.removeClient(this);
                running = false;
            }
        } else {
            logger.info("Client socket is closed. Unable to send message: " + message);
            if (running) {
                running = false;
                server.removeClient(this);
            }
        }
    }

    /**
     * Sends a PING message to the corresponding client.
     * If the client does not respond within the timeout period, the client is disconnected.
     */
    public void sendPing() {
        if(System.currentTimeMillis() - lastPingTime > SETTINGS.Config.TIMEOUT.getValue()) {
            logger.warning("Client timed out: " + (socket != null ? socket.getRemoteSocketAddress() : "unknown"));
            closeResources();
            server.removeClient(this);
            running = false;
        } else {
            sendMessage("PING$");
        }
    }

    /**
     * Sends a global chat message to all players in the lobby.
     * @param cmd the command to send
     */
    public void sendGlobalChatMessage(Command cmd) {
        if (currentLobby != null) {
            currentLobby.broadcastMessage(cmd.toString());
        } else {
            sendMessage("ERR:106;NOT_IN_LOBBY");
        }
    }

    /**
     * Processes a received message from the client.
     * @param received the received message String
     *                 The message is parsed into a Command object and processed accordingly.
     *                 If the command is invalid, an error message is sent to the client.
     *                 If the command is valid, it is processed according to the protocol.
     */
    @Override
    public void processMessage(String received) {
        // Update last ping time for any valid message
        lastPingTime = System.currentTimeMillis();

        if (received == null || received.trim().isEmpty()) {
            logger.warning("Received null or empty message");
            sendMessage("ERR0R:103;Null");
            return;
        }
        Command cmd = new Command(received);
        if (cmd.isValid()) {
            boolean answer = false; // Assume command does not have to be answered
            logger.info("Server processing " + cmd);

            NetworkProtocol.Commands command;
            try {
                command = NetworkProtocol.Commands.fromCommand(cmd.getCommand());
            } catch (IllegalArgumentException e) {
                logger.warning("Protocol-Unknown command: " + cmd.getCommand());
                return;
            }

            switch (command) {
                case CHATGLOBAL:
                    sendGlobalChatMessage(cmd);
                    break;
                case PING:
                    answer = true;
                    break;
                case TEST:
                    logger.info("TEST");
                    break;
                case OK:
                    break;
                case ERROR:
                    logger.info("Client sent an error command.");
                    break;
                case CREATELOBBY:
                    handleCreateLobby(cmd);
                    break;
                case JOIN:
                    handleJoinLobby(cmd);
                    break;
                case LEAVE:
                    handleLeaveLobby();
                    break;
                case START:
                    handleStartGame();
                    break;
                case CHANGENAME:
                    handleChangeName(cmd);
                    break;
                case REGISTER:
                    handleRegister(cmd);
                    break;
                case LISTLOBBIES:
                    handleListLobbies();
                    break;
                case EXIT:
                    logger.info("Client sent an exit command.");
                    handleLeaveLobby();
                    server.removeClient(this);
                    break;
                default: // Error case
                    logger.warning("Switch-Unknown command: " + cmd.getCommand());
            }
            if(answer) {
                sendMessage("OK$" + cmd.toString()); // Echo the command back to the client with an OK response
            } else {
                sendMessage("ERR$100;" + cmd.toString()); // Echo the command back to the client with an ERR response
            }
        } else {
            logger.warning("ClientHandler: Invalid command: " + cmd);
        }
    }

    /**
     * Closes the resources associated with the client handler.
     */
    public void closeResources() {
        try {
            if (out != null) {
                out.close();
                out = null;
            }
            if (in != null) {
                in.close();
                in = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            Logger.getLogger(ClientHandler.class.getName()).warning("Error closing resources: " + e.getMessage());
        }
    }

    /**
     * Returns the running status of the client handler.
     * @return true if the client handler is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Stops the client handler.
     */
    public void stop() {
        running = false;
    }

    /**
     * Returns the current lobby the client is in.
     * @return the current Lobby as a
     * @see Lobby
     */
    public Lobby getCurrentLobby() {
        return currentLobby;
    }

    /**
     * Sets the current lobby the client is in.
     * @param currentLobby the current lobby of the respective player
     * @see Lobby
     */
    public void setCurrentLobby(Lobby currentLobby) {
        this.currentLobby = currentLobby;
    }

    /**
     * This method handles the creation of a lobby.
     * @param cmd the transmitted command
     */
    private void handleCreateLobby(Command cmd) {
        String hostname = cmd.getArgs()[0]; // Falls wir später mal den Hostnamen speichern wollen -> könnte man in Lobby hinzufügen
        String lobbyId = cmd.getArgs()[1];
        int maxPlayers = 4; //currently, maxPlayers is set to 4
        Lobby lobby = server.createLobby(lobbyId, maxPlayers);
        if (lobby != null && lobby.addPlayer(this)) {
            currentLobby = lobby;
            sendMessage("OK$CREA$" + lobbyId);
        } else {
            sendMessage("ERR$106$LOBBY_CREATION_FAILED");
        }
    }

    /**
     * This method handles the listing of all lobbies.
     */
    public void handleListLobbies() {
        List<Lobby> lobbies = server.getLobbies();
        StringBuilder sb = new StringBuilder("Lobbies: ");
        if(lobbies.isEmpty()) {
            sb.append("No available lobbies. Create your own with /create");
        } else if(lobbies.size() == 1) {
            sb.append(lobbies.get(0).getId());
        } else {
            for (Lobby lobby : lobbies) {
                sb.append(lobby.getId()).append(", ");
            }
        }
        sendMessage(sb.toString());
    }

    /**
     * This method handles a player (client) joining a Lobby.
     * @param cmd the transmitted command
     */
    private void handleJoinLobby(Command cmd) {
        String lobbyId = cmd.getArgs()[1];
        Lobby lobby = server.getLobby(lobbyId);
        if (lobby != null && lobby.addPlayer(this)) {
            currentLobby = lobby; // Set the current lobby
            sendMessage("OK$JOIN$" + lobbyId);
        } else {
            sendMessage("ERR$106$JOIN_LOBBY_FAILED");
        }
    }

    /**
     * This method handles a player (client) exiting a Lobby.
      */
    private void handleLeaveLobby() {
        if (currentLobby != null && currentLobby.removePlayer(this)) {
            String lobbyId = currentLobby.getId();
            sendMessage("OK$LEAV$" + lobbyId);
            Lobby lobby = server.getLobby(lobbyId);
            if(lobby != null && lobby.isEmpty()) {
                server.removeLobby(lobby);
            }
            currentLobby = null; // Clear the current lobby reference
        } else {
            sendMessage("ERR$106$NOT_IN_LOBBY");
        }
    }

    /**
     * This method handles the starting of a game.
     */
    private void handleStartGame() {
        if (currentLobby != null && currentLobby.getPlayers().get(0) == this && currentLobby.startGame()) {
            sendMessage("OK$STRT");
            //TODO Start Game
        } else {
            sendMessage("ERR$106$CANNOT_START_GAME");
        }
    }

    /**
     * This method handles the registration of a player.
     * Gets called immediately when a player connects
     * @param cmd the transmitted command
     */
    private void handleRegister(Command cmd) {
        String playerName = cmd.getArgs()[0];
        if (!server.containsPlayerName(playerName)) {
            this.localPlayer = new Player(playerName);
            sendMessage("OK$RGST$" + playerName);
        }
        else {
            this.localPlayer = new Player(playerName+"2"); // Adds playerName2 as a new Player
            sendMessage("ERR$106$PLAYER_ALREADY_EXISTS"); // Tells Client to tell player about changeName
        }
    }

    /**
     * This method handles the changing of a player's name.
     * @param cmd the transmitted command
     */
    private void handleChangeName(Command cmd) {
        String newPlayerName = cmd.getArgs()[0];
        if (!server.containsPlayerName(newPlayerName)){
            sendMessage("OK$CHAN$" + newPlayerName);
            server.broadcast(localPlayer.getName() + " changed name to " + newPlayerName);
            localPlayer.setName(newPlayerName);
        }
        else {
            sendMessage("ERR$106$PLAYER_ALREADY_EXISTS");
        }
    }

    /**
     * This method returns the localPlayer variable assigned to the ClientHandler
     * @return the current localPlayer state
     */
    public Player getPlayer(){
        return localPlayer;
    }
}