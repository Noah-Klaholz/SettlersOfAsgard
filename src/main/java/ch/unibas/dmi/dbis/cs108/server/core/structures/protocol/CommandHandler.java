package ch.unibas.dmi.dbis.cs108.server.core.structures.protocol;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.networking.GameServer;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CommandHandler {
    Logger logger = Logger.getLogger(CommandHandler.class.getName());
    private final ClientHandler ch;
    private final GameServer server;
    private Lobby currentLobby;
    private Player localPlayer;

    /**
     * Constructor for the ClientHandler class.
     */
    public CommandHandler(ClientHandler clientHandler) {
        this.ch = clientHandler;
        this.localPlayer = ch.getPlayer();
        this.currentLobby = ch.getCurrentLobby();
        this.server = ch.getServer();
    }

    private void sendMessage(String message) {
        ch.sendMessage(message);
    }

    private void setCurrentLobby(Lobby lobby) {
        ch.setCurrentLobby(lobby);
        currentLobby = lobby;
    }

    private void setLocalPlayerName(String playerName) {
        this.localPlayer.setName(playerName);
        ch.getPlayer().setName(playerName);
    }

    private void setLocalPlayer(Player player) {
        this.localPlayer = player;
        ch.setPlayer(player);
    }

    private void joinLobby(Lobby lobby) {
        if (ch.getCurrentLobby() != null) {
            ch.getCurrentLobby().removePlayer(ch);
        }
        setCurrentLobby(lobby);
    }

    public void handleListPlayers(Command cmd) {
        String[] arg = cmd.getArgs();
        if (arg.length != 1) {
            sendMessage("ERR$101$INVALID_ARGUMENTS");
        } else {
            String list;
            if (arg[0].equals("LOBBY")) {
                if (currentLobby != null) {
                    list = currentLobby.listPlayers();
                    sendMessage(list);
                } else {
                    sendMessage("ERR$106$NOT_IN_LOBBY");
                }
            } else if (arg[0].equals("SERVER")) {
                list = server.listPlayers();
                sendMessage(list);
            } else {
                sendMessage("ERR$101$INVALID_ARGUMENTS");
            }

        }
    }

    /**
     * This method handles the creation of a lobby.
     *
     * @param cmd the transmitted command
     */
    public void handleCreateLobby(Command cmd) {
        if (currentLobby != null) {
            handleLeaveLobby();
        }
        String hostname = cmd.getArgs()[0]; // Falls wir später mal den Hostnamen speichern wollen -> könnte man in Lobby hinzufügen
        String lobbyId = cmd.getArgs()[1];
        int maxPlayers = 4; //currently, maxPlayers is set to 4
        Lobby lobby = server.createLobby(lobbyId, maxPlayers);
        if (lobby != null && lobby.addPlayer(ch)) {
            joinLobby(lobby);
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

        if (lobbies.isEmpty()) {
            sendMessage("Lobbies: No available lobbies. Create your own with /create");
            return;
        }

        String lobbyList = lobbies.stream()
                .map(lobby -> lobby.getId() + ":  " + lobby.getStatus())
                .collect(Collectors.joining("\n"));

        ch.sendMessage("Lobbies: \n" + lobbyList);
    }

    /**
     * This method handles a player (client) joining a Lobby.
     *
     * @param cmd the transmitted command
     */
    public void handleJoinLobby(Command cmd) {
        if (currentLobby != null) {
            handleLeaveLobby();
        }
        String lobbyId = cmd.getArgs()[1];
        Lobby lobby = server.getLobby(lobbyId);
        if (lobby != null && lobby.addPlayer(ch)) {
            joinLobby(lobby);
            sendMessage("OK$JOIN$" + lobbyId);
        } else {
            sendMessage("ERR$106$JOIN_LOBBY_FAILED");
        }
    }

    /**
     * This method handles a player (client) exiting a Lobby.
     */
    public void handleLeaveLobby() {
        if (currentLobby != null && currentLobby.removePlayer(ch)) {
            String lobbyId = currentLobby.getId();
            sendMessage("OK$LEAV$" + lobbyId);
            Lobby lobby = server.getLobby(lobbyId);
            if (lobby != null && lobby.isEmpty()) {
                server.removeLobby(lobby);
            }
            setCurrentLobby(null);
        } else {
            sendMessage("ERR$106$NOT_IN_LOBBY");
        }
    }

    /**
     * This method handles the starting of a game.
     */
    public void handleStartGame() {
        if (currentLobby != null && currentLobby.getPlayers().get(0) == ch && currentLobby.startGame()) {
            sendMessage("OK$STRT");
            //TODO Start Game
        } else {
            sendMessage("ERR$106$CANNOT_START_GAME");
        }
    }

    /**
     * This method handles the registration of a player.
     * Gets called immediately when a player connects
     *
     * @param cmd the transmitted command
     */
    public void handleRegister(Command cmd) {
        String playerName = cmd.getArgs()[0];
        synchronized (server) {
            if (!server.containsPlayerName(playerName)) {
                setLocalPlayer(new Player(playerName));
                sendMessage("OK$RGST$" + playerName);
            } else {
                // Find a unique name by adding numbers
                String uniqueName = playerName;
                int suffix = 2;
                while (server.containsPlayerName(uniqueName)) {
                    uniqueName = playerName + suffix++;
                }

                setLocalPlayer(new Player(uniqueName));
                sendMessage("ERR$106$PLAYER_ALREADY_EXISTS$" + uniqueName);
            }
        }
    }

    /**
     * This method handles the changing of a player's name.
     *
     * @param cmd the transmitted command
     */
    public void handleChangeName(Command cmd) {
        String newPlayerName = cmd.getArgs()[0];

        synchronized (server) {
            if (!server.containsPlayerName(newPlayerName)) {
                sendMessage("OK$CHAN$" + newPlayerName);
                server.broadcast(localPlayer.getName() + " changed name to " + newPlayerName);
                setLocalPlayerName(newPlayerName);
            } else {
                // Generate unique name
                String uniqueName = newPlayerName;
                int suffix = 2;
                while (server.containsPlayerName(uniqueName)) {
                    uniqueName = newPlayerName + suffix++;
                }

                setLocalPlayerName(uniqueName);
                sendMessage("ERR$106$PLAYER_ALREADY_EXISTS$" + uniqueName);
            }
        }
    }

    /**
     * This method handles the sending of a private message to another player.
     *
     * @param cmd the transmitted command
     */
    public void handlePrivateMessage(Command cmd) {
        String[] parts = cmd.getArgs();
        String senderName = parts[0];
        String receiverName = parts[1];
        if (receiverName.equals(senderName)) {
            sendMessage("ERR$106$CANNOT_WHISPER_TO_SELF");
            return;
        }
        String message = parts[2];
        if (server.containsPlayerName(receiverName)) {
            server.getClients().forEach(client -> {
                if (client.isRunning() && client.getPlayerName().equals(receiverName)) {
                    client.sendMessage("<Whisper>" + senderName + ": " + message);
                    sendMessage("OK$CHTP$");
                }
            });
        } else {
            sendMessage("ERR$105$NO_PLAYER_FOUND_PRIVATE_MESSAGE$" + senderName);
        }

    }

    public void handleLobbyMessage(Command cmd) {
        String senderName = cmd.getArgs()[0];
        String message = cmd.getArgs()[1];
        if (currentLobby != null) {
            currentLobby.broadcastMessage(senderName + ": " + message);
            sendMessage("OK$CHTL$");
        } else {
            sendMessage("ERR$106$NOT_IN_LOBBY");
        }
    }

    public void handleGlobalChatMessage(Command cmd) {
        if (cmd.getCommand().equals("CHTL")) {
            String command = cmd.toString().replace("CHTL", "CHTG").trim();
            ch.sendGlobalChatMessage(new Command(command));
        } else {
            ch.sendGlobalChatMessage(cmd);
        }
    }
    
    /**
     * Handles start turn command from client
     */
    public void handleStartTurn() {
        // TODO: Implement start turn logic
    }

    /**
     * Handles end turn command from client
     */
    public void handleEndTurn() {
        // TODO: Implement end turn logic
    }

    /**
     * Handles stats request from client
     */
    public void handleStats() {
        // TODO: Implement stats logic
    }

    /**
     * Handles synchronization request from client
     */
    public void handleSynchronize() {
        // TODO: Implement synchronization logic
    }

    /**
     * Handles request for game status
     * @param cmd the transmitted command
     */
    public void handleGetGameStatus(Command cmd) {
        try {
            String playerName = cmd.getArgs()[0];
            // TODO: Implement game status retrieval logic
        } catch (Exception e) {
            logger.severe("Failed to handle game status request: " + e.getMessage());
            sendMessage("ERR$106$GAME_STATUS_REQUEST_FAILED");
        }
    }

    /**
     * Handles request for price information
     * @param cmd the transmitted command
     */
    public void handleGetPrices(Command cmd) {
        try {
            String playerName = cmd.getArgs()[0];
            // TODO: Implement price retrieval logic
        } catch (Exception e) {
            logger.severe("Failed to handle price request: " + e.getMessage());
            sendMessage("ERR$106$PRICE_REQUEST_FAILED");
        }
    }

    /**
     * Handles buy tile request from client
     * @param cmd the transmitted command
     */
    public void handleBuyTile(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length < 3) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$BUY_TILE");
                return;
            }
            String playerName = args[0];
            int x = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);
            // TODO: Implement buy tile logic
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates: " + e.getMessage());
            sendMessage("ERR$101$INVALID_COORDINATES");
        } catch (Exception e) {
            logger.severe("Failed to handle buy tile request: " + e.getMessage());
            sendMessage("ERR$106$BUY_TILE_FAILED");
        }
    }

    /**
     * Handles place structure request from client
     * @param cmd the transmitted command
     */
    public void handlePlaceStructure(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length < 3) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$PLACE_STRUCTURE");
                return;
            }
            String playerName = args[0];
            int x = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);
            // TODO: Implement place structure logic
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates: " + e.getMessage());
            sendMessage("ERR$101$INVALID_COORDINATES");
        } catch (Exception e) {
            logger.severe("Failed to handle place structure request: " + e.getMessage());
            sendMessage("ERR$106$PLACE_STRUCTURE_FAILED");
        }
    }

    /**
     * Handles use structure request from client
     * @param cmd the transmitted command
     */
    public void handleUseStructure(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length < 3) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$USE_STRUCTURE");
                return;
            }
            String playerName = args[0];
            int x = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);
            // TODO: Implement use structure logic
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates: " + e.getMessage());
            sendMessage("ERR$101$INVALID_COORDINATES");
        } catch (Exception e) {
            logger.severe("Failed to handle use structure request: " + e.getMessage());
            sendMessage("ERR$106$USE_STRUCTURE_FAILED");
        }
    }

    /**
     * Handles upgrade statue request from client
     * @param cmd the transmitted command
     */
    public void handleUpgradeStatue(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length < 3) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$UPGRADE_STATUE");
                return;
            }
            String playerName = args[0];
            int x = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);
            // TODO: Implement upgrade statue logic
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates: " + e.getMessage());
            sendMessage("ERR$101$INVALID_COORDINATES");
        } catch (Exception e) {
            logger.severe("Failed to handle upgrade statue request: " + e.getMessage());
            sendMessage("ERR$106$UPGRADE_STATUE_FAILED");
        }
    }

    /**
     * Handles use statue request from client
     * @param cmd the transmitted command
     */
    public void handleUseStatue(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length < 3) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$USE_STATUE");
                return;
            }
            String playerName = args[0];
            int x = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);
            // TODO: Implement use statue logic
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates: " + e.getMessage());
            sendMessage("ERR$101$INVALID_COORDINATES");
        } catch (Exception e) {
            logger.severe("Failed to handle use statue request: " + e.getMessage());
            sendMessage("ERR$106$USE_STATUE_FAILED");
        }
    }

    /**
     * Handles use player artifact request from client
     * @param cmd the transmitted command
     */
    public void handleUsePlayerArtifact(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length < 2) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$USE_PLAYER_ARTIFACT");
                return;
            }
            String playerName = args[0];
            int artifactId = Integer.parseInt(args[1]);
            // TODO: Implement use player artifact logic
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse artifact ID: " + e.getMessage());
            sendMessage("ERR$101$INVALID_ARTIFACT_ID");
        } catch (Exception e) {
            logger.severe("Failed to handle use player artifact request: " + e.getMessage());
            sendMessage("ERR$106$USE_PLAYER_ARTIFACT_FAILED");
        }
    }

    /**
     * Handles use trap request from client
     * @param cmd the transmitted command
     */
    public void handleUseTrap(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length < 4) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$USE_TRAP");
                return;
            }
            String playerName = args[0];
            int x = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);
            String trapId = args[3];
            // TODO: Implement use trap logic
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates: " + e.getMessage());
            sendMessage("ERR$101$INVALID_COORDINATES");
        } catch (Exception e) {
            logger.severe("Failed to handle use trap request: " + e.getMessage());
            sendMessage("ERR$106$USE_TRAP_FAILED");
        }
    }

    /**
     * Handles buy hex field request from client
     * @param cmd the transmitted command
     */
    public void handleBuyHexField(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length < 1) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$BUY_HEX_FIELD");
                return;
            }
            String playerName = args[0];
            // TODO: Implement buy hex field logic
        } catch (Exception e) {
            logger.severe("Failed to handle buy hex field request: " + e.getMessage());
            sendMessage("ERR$106$BUY_HEX_FIELD_FAILED");
        }
    }

    /**
     * Handles build structure request from client
     * @param cmd the transmitted command
     */
    public void handleBuildStructure(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length < 1) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$BUILD_STRUCTURE");
                return;
            }
            String playerName = args[0];
            // TODO: Implement build structure logic
        } catch (Exception e) {
            logger.severe("Failed to handle build structure request: " + e.getMessage());
            sendMessage("ERR$106$BUILD_STRUCTURE_FAILED");
        }
    }

    /**
     * Handles upgrade structure request from client
     * @param cmd the transmitted command
     */
    public void handleUpgradeStructure(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length < 1) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$UPGRADE_STRUCTURE");
                return;
            }
            String playerName = args[0];
            // TODO: Implement upgrade structure logic
        } catch (Exception e) {
            logger.severe("Failed to handle upgrade structure request: " + e.getMessage());
            sendMessage("ERR$106$UPGRADE_STRUCTURE_FAILED");
        }
    }

    /**
     * Handles use artifact request from client
     * @param cmd the transmitted command
     */
    public void handleUseArtifact(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length < 1) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$USE_ARTIFACT");
                return;
            }
            String playerName = args[0];
            // TODO: Implement use artifact logic
        } catch (Exception e) {
            logger.severe("Failed to handle use artifact request: " + e.getMessage());
            sendMessage("ERR$106$USE_ARTIFACT_FAILED");
        }
    }
}
