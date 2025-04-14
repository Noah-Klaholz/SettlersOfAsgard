package ch.unibas.dmi.dbis.cs108.server.core.structures.protocol;

import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.networking.GameServer;

import java.util.List;
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
     * The GameLogic instance that this CommandHandler is associated with.
     */
    private GameLogic gameLogic;
    /**
     * The current Lobby that this CommandHandler is associated with.
     */
    private Lobby currentLobby;
    /**
     * The local player that this CommandHandler is associated with.
     */
    private Player localPlayer;
    /**
     * The name of the player that this CommandHandler is associated with.
     */
    private String playerName;

    /**
     * Constructor for the ClientHandler class.
     *
     * @param clientHandler the ClientHandler instance
     */
    public CommandHandler(ClientHandler clientHandler) {
        this.ch = clientHandler;
        this.localPlayer = ch.getPlayer();
        this.currentLobby = ch.getCurrentLobby();
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
        currentLobby = lobby;
    }

    /**
     * This method sets the name of the local player.
     *
     * @param playerName the name to set
     */
    private void setLocalPlayerName(String playerName) {
        this.localPlayer.setName(playerName);
        this.playerName = playerName;
        ch.getPlayer().setName(playerName);
    }

    /**
     * This method sets the local player for this CommandHandler.
     *
     * @param player the player to set
     */
    private void setLocalPlayer(Player player) {
        this.localPlayer = player;
        this.playerName = player.getName();
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
                logger.info("Duplicate player registered: " + playerName);
                setLocalPlayer(new Player(uniqueName));
                sendMessage("OK$RGST$" + uniqueName);
                sendMessage("ERR$106$PLAYER_ALREADY_EXISTS$" + uniqueName);
            }
        }
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
                .map(lobby -> lobby.getId() + ":  " + lobby.getStatus())
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
        if (currentLobby != null) {
            handleLeaveLobby();
        }
        String hostname = cmd.getArgs()[0]; // Falls wir später mal den Hostnamen speichern wollen -> könnte man in Lobby hinzufügen
        String lobbyId = cmd.getArgs()[1];
        int maxPlayers = 4; //currently, maxPlayers is set to 4
        Lobby lobby = server.createLobby(lobbyId, maxPlayers);
        if (lobby != null && lobby.addPlayer(ch)) {
            joinLobby(lobby);
            return true;
        } else {
            sendMessage("ERR$106$LOBBY_CREATION_FAILED");
        }
        return false;
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
        if (currentLobby != null && currentLobby.removePlayer(ch)) {
            String oldLobbyId = currentLobby.getId();
            Lobby oldLobby = server.getLobby(oldLobbyId);
            oldLobby.broadcastMessage("OK$LEAV$" + playerName + "$" + oldLobbyId);
            sendMessage("OK$LEAV$" + playerName + "$" + oldLobbyId);
            if (oldLobby.isEmpty()) {
                server.removeLobby(oldLobby);
            }
            setCurrentLobby(null);
        }
        if (lobby != null && lobby.addPlayer(ch)) {
            joinLobby(lobby);
            currentLobby.broadcastMessage("OK$JOIN$" + playerName + "$" + lobbyId);
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
        if (currentLobby != null && currentLobby.removePlayer(ch)) {
            String lobbyId = currentLobby.getId();
            currentLobby.broadcastMessage("OK$LEAV$" + playerName + "$" + lobbyId);
            sendMessage("OK$LEAV$" + playerName + "$" + lobbyId);
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
        String senderName = cmd.getArgs()[0];
        String message = cmd.getArgs()[1];
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
        String com = cmd.toString();
        com = com.replace("CHTL$", "CHTG$");
        ch.sendGlobalChatMessage(new Command(com,localPlayer));
        return true;
    }

    /**
     * This method handles the starting of a game.
     *
     * @return true if the game was started successfully, false otherwise
     */
    public boolean handleStartGame() {
        if (ch.getCurrentLobby() != null && ch.getCurrentLobby().startGame()) {
            this.currentLobby = ch.getCurrentLobby();
            this.gameLogic = currentLobby.getGameLogic();
            String startPlayerName = gameLogic.getTurnManager().getPlayerTurn();
            currentLobby.broadcastMessage("STRT$" + startPlayerName);
            return true;
        } else {
            System.out.println("ERR$106$NOT_IN_LOBBY");
            sendMessage("ERR$106$CANNOT_START_GAME");
            return false;
        }
    }

    /**
     * This method returns the current game logic.
     *
     * @return the current game logic
     */
    public GameLogic getGameLogic() {
        // Refresh gameLogic if it's null but the lobby has one
        if (gameLogic == null && currentLobby != null) {
            gameLogic = currentLobby.getGameLogic();
        }
        return gameLogic;
    }
}