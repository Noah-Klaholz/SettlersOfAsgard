package ch.unibas.dmi.dbis.cs108.server.networking;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.core.structures.protocol.CommandHandler;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * The ClientHandler class is responsible for handling the communication between the server and a client.
 * It serves as an interface for the server to access the client's socket and send messages to the client.
 * It implements the Runnable interface to allow for multi-threading.
 * It also implements the CommunicationAPI interface to allow for communication with the server.
 */
public class ClientHandler implements Runnable, CommunicationAPI {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private final CommandHandler ch; // Reference to a CommandHandler
    protected GameServer server; // Reference to the GameServer
    protected Lobby currentLobby = null;
    protected Player localPlayer = null;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private long lastPingTime = System.currentTimeMillis();
    private boolean running;


    /**
     * Constructor for the ClientHandler class.
     *
     * @param socket the client's socket
     * @param server the GameServer
     */
    public ClientHandler(Socket socket, GameServer server) {
        logger.setFilter(new PingFilter());
        this.socket = socket;
        this.server = server;
        this.running = true;
        this.ch = new CommandHandler(this);
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

    public void startGame() {
        sendMessage(NetworkProtocol.Commands.START+"$");
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
     * Sends a String message to the client.
     *
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
     * Sends a global chat message to all players in the server.
     *
     * @param cmd the command to send
     */
    public void sendGlobalChatMessage(Command cmd) {
        server.broadcast(cmd.toString());
    }

    /**
     * Sends a PING message to the corresponding client.
     * If the client does not respond within the timeout period, the client is disconnected.
     */
    public void sendPing() {
        if (System.currentTimeMillis() - lastPingTime > SETTINGS.Config.TIMEOUT.getValue()) {
            logger.warning("Client timed out: " + (socket != null ? socket.getRemoteSocketAddress() : "unknown"));
            closeResources();
            server.removeClient(this);
            running = false;
        } else {
            sendMessage("PING$");
        }
    }

    /**
     * Returns the running status of the client handler.
     *
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


    public GameServer getServer() {
        return server;
    }

    /**
     * Returns the current lobby the client is in.
     *
     * @return the current Lobby as a Lobby object
     * @see Lobby
     */
    public Lobby getCurrentLobby() {
        return currentLobby;
    }

    /**
     * Sets the current lobby the client is in.
     *
     * @param currentLobby the current lobby of the respective player
     * @see Lobby
     */
    public void setCurrentLobby(Lobby currentLobby) {
        this.currentLobby = currentLobby;
    }

    /**
     * This method returns the localPlayer variable assigned to the ClientHandler
     *
     * @return the current localPlayer state
     */
    public Player getPlayer() {
        return localPlayer;
    }

    /**
     * Sets the local player to the given Player argument
     *
     * @param player
     */
    public void setPlayer(Player player) {
        localPlayer = player;
    }

    /**
     * This method returns the name of the localPlayer assigned to the ClientHandler
     *
     * @return the name of the current localPlayer
     */
    public String getPlayerName() {
        return localPlayer.getName();
    }

    /**
     * Processes a received message from the client.
     *
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
            sendMessage("ERR0R$103$Null");
            return;
        }
        Command cmd = new Command(received);
        if (cmd.isValid()) {
            boolean answer = true; // Assume command has to be answered
            boolean worked = true; // Assume command was processed successfully
            logger.info("Server processing " + cmd);

            NetworkProtocol.Commands command;
            try {
                command = NetworkProtocol.Commands.fromCommand(cmd.getCommand());
            } catch (IllegalArgumentException e) {
                logger.warning("Protocol-Unknown command: " + cmd.getCommand());
                return;
            }

            switch (command) {
                case CHATLOBBY:
                    System.out.println("CHATLOBBY: " + currentLobby);
                    if (currentLobby == null || getCurrentLobby() == null) {
                        worked = ch.handleGlobalChatMessage(cmd);
                    } else {
                        worked = ch.handleLobbyMessage(cmd);
                    }
                    answer = false;
                    break;
                case CHATPRIVATE:
                    answer = false;
                    worked = ch.handlePrivateMessage(cmd);
                    break;
                case CHATGLOBAL:
                    answer = false;
                    worked = ch.handleGlobalChatMessage(cmd);
                    break;
                case PING:
                    break;
                case TEST:
                    answer = false;
                    logger.info("TEST");
                    break;
                case OK:
                    answer = false;
                    break;
                case ERROR:
                    answer = false;
                    logger.info("Client sent an error command.");
                    break;
                case CREATELOBBY:
                    worked = ch.handleCreateLobby(cmd);
                    break;
                case JOIN:
                    answer = false;
                    worked = ch.handleJoinLobby(cmd);
                    break;
                case LEAVE:
                    answer = false;
                    worked = ch.handleLeaveLobby();
                    break;
                case START:
                    worked = ch.handleStartGame();
                    break;
                case CHANGENAME:
                    answer = false;
                    worked = ch.handleChangeName(cmd);
                    break;
                case REGISTER:
                    answer = false;
                    worked = ch.handleRegister(cmd);
                    break;
                case LISTLOBBIES:
                    answer = false;
                    worked = ch.handleListLobbies();
                    break;
                case LISTPLAYERS:
                    answer = false;
                    worked = ch.handleListPlayers(cmd);
                    break;
                case EXIT:
                    logger.info("Client sent an exit command.");
                    worked = ch.handleLeaveLobby();
                    server.removeClient(this);
                    break;
                case STARTTURN:
                    answer = false;
                    worked = ch.handleStartTurn();
                    break;
                case ENDTURN:
                    answer = false;
                    worked = ch.handleEndTurn();
                    break;
                case SYNCHRONIZE:
                    worked = ch.handleSynchronize();
                    break;
                case GETGAMESTATUS:
                    answer = false;
                    worked = ch.handleGetGameStatus();
                    break;
                case GETPRICES:
                    answer = false;
                    worked = ch.handleGetPrices();
                    break;
                case BUYTILE:
                    worked = ch.handleBuyTile(cmd);
                    break;
                case BUYSTRUCTURE:
                    answer = false;
                    worked = ch.handleBuyStructure(cmd);
                    break;
                case PLACESTRUCTURE:
                    answer = false;
                    worked = ch.handlePlaceStructure(cmd);
                    break;
                case USESTRUCTURE:
                    worked = ch.handleUseStructure(cmd);
                    break;
                case UPGRADESTATUE:
                    answer = false;
                    worked = ch.handleUpgradeStatue(cmd);
                    break;
                case USESTATUE:
                    answer = false;
                    worked = ch.handleUseStatue(cmd);
                    break;
                case USEPLAYERARTIFACT:
                    answer = false;
                    worked = ch.handleUsePlayerArtifact(cmd);
                    break;
                case USEFIELDARTIFACT:
                    answer = false;
                    worked = ch.handleUseFieldArtifact(cmd);
                    break;
                case BUYSTATUE:
                    answer = false;
                    worked = ch.handleBuyStatue(cmd);
                    break;
                default: // Error case
                    logger.warning("Switch-Unknown command: " + cmd.getCommand());
            }
            if (answer && worked) {
                sendMessage("OK$" + cmd); // Echo the command back to the client with an OK response
            }
        } else {
            logger.warning("ClientHandler: Invalid command: " + cmd);
        }
    }
}