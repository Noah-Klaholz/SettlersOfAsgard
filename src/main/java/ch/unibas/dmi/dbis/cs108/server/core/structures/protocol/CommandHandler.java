package ch.unibas.dmi.dbis.cs108.server.core.structures.protocol;

import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.core.model.Leaderboard;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.networking.GameServer;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * CommandHandler class handles the commands sent by the client.
 * It processes the commands and interacts with the server and game logic.
 */
public class CommandHandler {

    /**
     * The ClientHandler instance that this CommandHandler is associated with.
     */
    private final ClientHandler ch;
    /**
     * The GameServer instance that this CommandHandler is associated with.
     */
    private final GameServer server;
    /**
     * The logger for this class.
     */
    Logger logger = Logger.getLogger(CommandHandler.class.getName());

    /**
     * Constructor for the ClientHandler class.
     *
     * @param clientHandler the ClientHandler instance
     */
    public CommandHandler(ClientHandler clientHandler) {
        this.ch = clientHandler;
        this.server = ch.getServer();
    }

    /**
     * This method sends the message and calls the appropriate handler method.
     *
     * @param message the message to send
     */
    private void sendMessage(String message) {
        ch.sendMessage(message);
    }

    /**
     * This method sets the current lobby for this CommandHandler.
     *
     * @param lobby the lobby to set
     */
    private void setCurrentLobby(Lobby lobby) {
        ch.setCurrentLobby(lobby);
    }

    /**
     * This method sets the name of the local player.
     *
     * @param playerName the name to set
     */
    private void setLocalPlayerName(String playerName) {
        Lobby currentLobby = ch.getCurrentLobby();
        Player localPlayer = ch.getPlayer();
        if (currentLobby != null && Objects.equals(currentLobby.getStatus(), Lobby.LobbyStatus.IN_GAME.getStatus())) {
            Logger.getGlobal().info("Setting lobby playername to " + playerName);
            currentLobby.changeName(localPlayer.getName(), playerName);
        }
        localPlayer.setName(playerName);
        ch.getPlayer().setName(playerName);
    }

    /**
     * This method sets the local player for this CommandHandler.
     *
     * @param player the player to set
     */
    private void setLocalPlayer(Player player) {
        ch.setPlayer(player);
    }

    /**
     * This method sets the current lobby for this CommandHandler.
     *
     * @param lobby the lobby to set
     */
    private void joinLobby(Lobby lobby) {
        if (ch.getCurrentLobby() != null) {
            ch.getCurrentLobby().removePlayer(ch);
        }
        setCurrentLobby(lobby);
    }

    /**
     * This method handles the registration of a player.
     * Gets called immediately when a player connects
     *
     * @param cmd the transmitted command
     * @return true if the command was handled successfully, false otherwise
     */
    public boolean handleRegister(Command cmd) {
        String playerName = cmd.getArgs()[0].toLowerCase();
        synchronized (server) {
            if (!server.containsPlayerName(playerName)) {
                logger.info("player registered: " + playerName);
                setLocalPlayer(new Player(playerName));
                sendMessage("OK$RGST$" + playerName);
            } else {
                // Find a unique name by adding numbers
                String uniqueName = playerName;
                int suffix = 2;
                while (server.containsPlayerName(uniqueName)) {
                    uniqueName = playerName + suffix++;
                }
                logger.info("Duplicate player registered: " + uniqueName);
                setLocalPlayer(new Player(uniqueName));
                sendMessage("OK$RGST$" + uniqueName);
                sendMessage("ERR$106$PLAYER_ALREADY_EXISTS$" + playerName);
            }
        }
        return true;
    }

    /**
     * This method handles the disconnection of a player.
     *
     * @return true if the command was handled successfully, false otherwise
     */
    public boolean handleExit() {
        handleLeaveLobby();
        server.removeClient(ch);
        sendMessage("OK$EXIT" + ch.getPlayerName());
        return true;
    }

    /**
     * This method handles the changing of a player's name.
     *
     * @param cmd the transmitted command
     * @return true if the command was handled successfully, false otherwise
     */
    public boolean handleChangeName(Command cmd) {
        String newPlayerName = cmd.getArgs()[0].toLowerCase();
        Player localPlayer = ch.getPlayer();

        synchronized (server) {
            if (!server.containsPlayerName(newPlayerName)) {
                sendMessage("OK$CHAN$" + localPlayer.getName() + "$" + newPlayerName);
                server.broadcast("CHAN$" + localPlayer.getName() + "$" + newPlayerName);
                setLocalPlayerName(newPlayerName);
                return true;
            } else {
                // Generate unique name
                String uniqueName = newPlayerName;
                int suffix = 2;
                while (server.containsPlayerName(uniqueName)) {
                    uniqueName = newPlayerName + suffix++;
                }

                setLocalPlayerName(uniqueName);
                sendMessage("ERR$106$PLAYER_ALREADY_EXISTS$" + uniqueName);
                server.broadcast("OK$CHAN$" + localPlayer.getName() + "$" + uniqueName);

            }
        }
        return false;
    }

    /**
     * This method handles the listing of players in a lobby or on the server.
     *
     * @param cmd the transmitted command
     * @return true if the command was handled successfully, false otherwise
     */
    public boolean handleListPlayers(Command cmd) {
        String[] arg = cmd.getArgs();
        if (!(arg.length >= 1)) {
            sendMessage("ERR$101$INVALID_ARGUMENTS");
        } else {
            String list;
            if (arg[0].equals("LOBBY") && arg.length == 2) {
                Lobby lobby = server.getLobby(arg[1]);
                if (lobby != null) {
                    list = lobby.listPlayers();
                    sendMessage("OK$LSTP$LOBBY$" + lobby.getId() + "$" + list);
                    return true;
                } else {
                    sendMessage("ERR$106$NOT_IN_LOBBY");
                }
            } else if (arg[0].equals("SERVER")) {
                list = server.listPlayers();
                sendMessage("OK$LSTP$SERVER$" + list);
            } else {
                sendMessage("ERR$101$INVALID_ARGUMENTS");
            }
        }
        return false;
    }

    /**
     * This method handles the listing of all lobbies.
     *
     * @return true if the command was handled successfully, false otherwise
     */
    public boolean handleListLobbies() {
        List<Lobby> lobbies = server.getLobbies();

        if (lobbies.isEmpty()) {
            ch.sendMessage("OK$LIST$No available lobbies. Create your own with /create");
            return true;
        }

        String lobbyList = lobbies.stream()
                .map(lobby -> lobby.getId() + ":" + lobby.getPlayers().size() + ":" + lobby.getMaxPlayers() + ":" + lobby.getStatus() + ":" + lobby.getHostName())
                .collect(Collectors.joining("%"));

        System.out.println(lobbyList);
        ch.sendMessage("OK$LIST$" + lobbyList);
        return true;
    }

    /**
     * This method handles the creation of a lobby.
     *
     * @param cmd the transmitted command
     * @return true if the command was handled successfully, false otherwise
     */
    public boolean handleCreateLobby(Command cmd) {
        if (ch.getCurrentLobby() != null) {
            handleLeaveLobby();
        }
        String lobbyId = cmd.getArgs()[1];
        int maxPlayers = Integer.parseInt(cmd.getArgs()[2]);
        Lobby lobby = server.createLobby(lobbyId, maxPlayers);
        if (lobby == null) {
            return false;
        }
        return handleJoinLobby(new Command(CommunicationAPI.NetworkProtocol.Commands.JOIN.getCommand() + "$" + ch.getPlayerName() + "$" + lobbyId, ch.getPlayer()));
    }

    /**
     * This method handles a player (client) joining a Lobby.
     *
     * @param cmd the transmitted command
     * @return true if the command was handled successfully, false otherwise
     */
    public boolean handleJoinLobby(Command cmd) {
        String lobbyId = cmd.getArgs()[1];
        Lobby lobby = server.getLobby(lobbyId);
        Lobby currentLobby = ch.getCurrentLobby();
        Player localPlayer = ch.getPlayer();
        if (currentLobby != null && currentLobby.removePlayer(ch)) {
            String oldLobbyId = currentLobby.getId();
            Lobby oldLobby = server.getLobby(oldLobbyId);
            oldLobby.broadcastMessage("OK$LEAV$" + localPlayer.getName() + "$" + oldLobbyId);
            sendMessage("OK$LEAV$" + localPlayer.getName() + "$" + oldLobbyId);
            if (oldLobby.isEmpty()) {
                server.removeLobby(oldLobby);
            }
            setCurrentLobby(null);
        }
        if (lobby != null && lobby.addPlayer(ch)) {
            joinLobby(lobby);
            Lobby joinedLobby = ch.getCurrentLobby();
            joinedLobby.broadcastMessage("OK$JOIN$" + lobbyId + "$" +
                    joinedLobby.getPlayers().stream()
                            .map(ClientHandler::getPlayerName)
                            .collect(Collectors.joining("%"))
                    + "$" +
                    (joinedLobby.getHostName().equals(localPlayer.getName()) ? "true" : "false"));
            return true;
        } else {
            sendMessage("ERR$106$JOIN_LOBBY_FAILED");
            return false;
        }
    }

    /**
     * This method handles a player (client) exiting a Lobby.
     *
     * @return true if the command was handled successfully, false otherwise
     */
    public boolean handleLeaveLobby() {
        Lobby currentLobby = ch.getCurrentLobby();
        Player localPlayer = ch.getPlayer();
        if (currentLobby != null && currentLobby.removePlayer(ch)) {
            String lobbyId = currentLobby.getId();
            currentLobby.broadcastMessage("OK$LEAV$" + localPlayer.getName() + "$" + lobbyId);
            sendMessage("OK$LEAV$" + localPlayer.getName() + "$" + lobbyId);
            Lobby lobby = server.getLobby(lobbyId);
            if (lobby != null && lobby.isEmpty()) {
                server.removeLobby(lobby);
            }
            setCurrentLobby(null);
            return true;
        } else {
            sendMessage("ERR$106$NOT_IN_LOBBY");
            return false;
        }
    }

    /**
     * Handles the reconnection of the client.
     *
     * @return true if the reconnection was successful, false otherwise.
     */
    public boolean handleReconnect() {
        if (ch.isDisconnected) {
            ch.markConnected();
            return true;
        }
        return false;
    }

    /**
     * This method handles the sending of a private message to another player.
     *
     * @param cmd the transmitted command
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean handlePrivateMessage(Command cmd) {
        String[] parts = cmd.getArgs();
        String senderName = parts[0];
        String receiverName = parts[1];
        if (receiverName.equals(senderName)) {
            sendMessage("ERR$106$CANNOT_WHISPER_TO_SELF");
            return false;
        }
        String message = parts[2];
        if (server.containsPlayerName(receiverName)) {
            server.getClients().forEach(client -> {
                if (client.isRunning() && (client.getPlayerName().equals(receiverName)) || client.getPlayerName().equals(senderName)) {
                    client.sendMessage("CHTP$" + senderName + "$" + message);
                }
            });
            return true;
        } else {
            sendMessage("ERR$105$NO_PLAYER_FOUND_PRIVATE_MESSAGE$" + senderName);
        }
        return false;
    }

    /**
     * Handles the sending of a message to all players in the lobby.
     *
     * @param cmd the transmitted command
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean handleLobbyMessage(Command cmd) {
        String senderName = ch.getPlayerName();
        String message = cmd.getArgs()[1];
        Lobby currentLobby = ch.getCurrentLobby();
        if (currentLobby != null) {
            currentLobby.broadcastMessage("CHTL$" + senderName + "$" + message);
            return true;
        } else {
            sendMessage("ERR$106$NOT_IN_LOBBY");
        }
        return false;
    }

    /**
     * Handles the sending of a message to all players on the server.
     *
     * @param cmd the transmitted command
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean handleGlobalChatMessage(Command cmd) {
        String senderName = ch.getPlayerName();
        String message = cmd.getArgs()[1];
        ch.sendGlobalChatMessage("CHTG$" + senderName + "$" + message);
        return true;
    }

    /**
     * This method handles the starting of a game.
     *
     * @return true if the game was started successfully, false otherwise
     */
    public boolean handleStartGame() {
        Lobby currentLobby = ch.getCurrentLobby();
        if (currentLobby != null && currentLobby.startGame()) {
            String startPlayerName = currentLobby.getGameLogic().getGameState().getPlayerTurn();
            currentLobby.broadcastMessage("STRT$" + startPlayerName);
            currentLobby.broadcastMessage(currentLobby.getGameLogic().getGameState().createDetailedStatusMessage());
            return true;
        } else {
            System.out.println("ERR$106$CANNOT_START_GAME");
            sendMessage("ERR$106$CANNOT_START_GAME");
            return false;
        }
    }

    /**
     * This method handles the ending of a game.
     *
     * @return true if the game was ended successfully, false otherwise
     */
    public boolean handleGetLeaderboard() {
        Leaderboard leaderboard = server.getLeaderboard();
        sendMessage("OK$" + CommunicationAPI.NetworkProtocol.Commands.LEADERBOARD.getCommand() + "$" + leaderboard);
        return true;
    }

    /**
     * This method returns the current game logic.
     *
     * @return the current game logic
     */
    public GameLogic getGameLogic() {
        Lobby currentLobby = ch.getCurrentLobby();
        return (currentLobby != null) ? currentLobby.getGameLogic() : null;
    }
}
