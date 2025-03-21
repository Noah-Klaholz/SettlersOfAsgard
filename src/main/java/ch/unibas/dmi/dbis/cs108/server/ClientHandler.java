package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.SETTINGS;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientHandler implements Runnable, CommunicationAPI {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private long lastPingTime = System.currentTimeMillis();
    private GameServer server; // Reference to the GameServer
    private boolean running;
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private Lobby currentLobby;


    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        this.running = true;
        try {
            socket.setSoTimeout(5000); // 5 second timeout
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            logger.severe("Error setting up client handler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        String received;
        try {
            while ((received = in.readLine()) != null) {
                processMessage(received);
            }
        } catch (IOException e) {
            logger.info("Client disconnected unexpectedly: " + e.getMessage());
        } finally {
            closeResources();
            server.removeClient(this); // Notify the server to remove this client
        }
    }

    @Override
    public void sendMessage(String message) {
        if (socket != null && !socket.isClosed()) {
            out.println(message);
        } else {
            logger.info("Client socket is closed. Unable to send message: " + message);
        }
    }

    public void sendPing() {
        if(System.currentTimeMillis() - lastPingTime > SETTINGS.Config.TIMEOUT.getValue()) {
            logger.warning("Client timed out: " + socket.getRemoteSocketAddress());
            closeResources();
            server.removeClient(this);
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

    @Override
    public void processMessage(String received) {
        if (received == null || received.trim().isEmpty()) {
            logger.warning("Received null or empty message");
            sendMessage("ERR0R:103;Null");
            return;
        }
        Command cmd = new Command(received);
        if (cmd.isValid()) {
            boolean processed = true; // Assume command is processed, only change in default (error) case
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
                    lastPingTime = System.currentTimeMillis();
                    break;
                case TEST:
                    logger.info("TEST");
                    break;
                case OK:
                    processed = false;
                    break;
                case ERROR:
                    processed = false;
                    logger.info("Client sent an error command.");
                    break;
                case CREATELOBBY:
                    handleCreateLobby(cmd);
                    break;
                case JOIN:
                    handleJoinLobby(cmd);
                    break;
                case EXIT:
                    handleLeaveLobby();
                    break;
                case START:
                    handleStartGame();
                    break;
                default: // Error case
                    logger.warning("Switch-Unknown command: " + cmd.getCommand());
                    processed = false;
            }
            if(processed) {
                sendMessage("OK$" + cmd.toString()); // Echo the command back to the client with an OK response
            } else {
                sendMessage("ERR$100;" + cmd.toString()); // Echo the command back to the client with an ERR response
            }
        } else {
            logger.warning("Invalid command: " + cmd);
        }
    }

    public void closeResources() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            Logger.getLogger(ClientHandler.class.getName()).warning("Error closing resources: " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }

    /**
     * This method handles the creation of a lobby.
     * @param cmd the transmitted command
     */
    private void handleCreateLobby(Command cmd) {
        String lobbyId = cmd.getArgs()[0];
        int maxPlayers = 4; //currently, maxPlayers is set to 4
        Lobby lobby = server.createLobby(lobbyId, maxPlayers);
        if (lobby.addPlayer(this)) {
            currentLobby = lobby;
            sendMessage("OK$LOBBY_CREATED$" + lobbyId);
        } else {
            sendMessage("ERR$106$LOBBY_CREATION_FAILED");
        }
    }

    /**
     * This method handles a player (client) joining a Lobby.
     * @param cmd the transmitted command
     */
    private void handleJoinLobby(Command cmd) {
        String lobbyId = cmd.getArgs()[0];
        Lobby lobby = server.getLobby(lobbyId);
        if (lobby != null && lobby.addPlayer(this)) {
            currentLobby = lobby; // Set the current lobby
            sendMessage("OK$JOINED_LOBBY$" + lobbyId);
        } else {
            sendMessage("ERR$106$JOIN_LOBBY_FAILED");
        }
    }

    /**
     * This method handles a player (client) exiting a Lobby.
      */
    private void handleLeaveLobby() {
        if (currentLobby != null && currentLobby.removePlayer(this)) {
            sendMessage("OK$LEFT_LOBBY$" + currentLobby.getId());
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
            sendMessage("OK$GAME_STARTED");
        } else {
            sendMessage("ERR$106$CANNOT_START_GAME");
        }
    }
}