package ch.unibas.dmi.dbis.cs108.server.core.structures.protocol;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.server.core.Logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.core.State.GameState;
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
    private GameLogic gameLogic;
    private Lobby currentLobby;
    private Player localPlayer;
    private String playerName;

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
        this.playerName = playerName;
        ch.getPlayer().setName(playerName);
    }

    private void setLocalPlayer(Player player) {
        this.localPlayer = player;
        this.playerName = player.getName();
        ch.setPlayer(player);
    }

    private void joinLobby(Lobby lobby) {
        if (ch.getCurrentLobby() != null) {
            ch.getCurrentLobby().removePlayer(ch);
        }
        setCurrentLobby(lobby);
    }

    public boolean handleListPlayers(Command cmd) {
        String[] arg = cmd.getArgs();
        if (arg.length != 1) {
            sendMessage("ERR$101$INVALID_ARGUMENTS");
        } else {
            String list;
            if (arg[0].equals("LOBBY")) {
                if (currentLobby != null) {
                    list = currentLobby.listPlayers();
                    sendMessage("OK$LSTP$LOBBY$"+ list);
                    return true;
                } else {
                    sendMessage("ERR$106$NOT_IN_LOBBY");
                }
            } else if (arg[0].equals("SERVER")) {
                list = server.listPlayers();
                sendMessage("OK$LSTP$SERVER$"+ list);
            } else {
                sendMessage("ERR$101$INVALID_ARGUMENTS");
            }
        }
        return false;
    }

    /**
     * This method handles the creation of a lobby.
     *
     * @param cmd the transmitted command
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
     * This method handles the listing of all lobbies.
     */
    public boolean handleListLobbies() {
        List<Lobby> lobbies = server.getLobbies();

        if (lobbies.isEmpty()) {
            sendMessage("Lobbies: No available lobbies. Create your own with /create");
            return true;
        }

        String lobbyList = lobbies.stream()
                .map(lobby -> lobby.getId() + ":  " + lobby.getStatus())
                .collect(Collectors.joining("\n"));

        ch.sendMessage("OK$LIST$" + lobbyList);
        return true;
    }

    /**
     * This method handles a player (client) joining a Lobby.
     *
     * @param cmd the transmitted command
     */
    public boolean handleJoinLobby(Command cmd) {
        if (currentLobby != null) {
            handleLeaveLobby();
        }
        String lobbyId = cmd.getArgs()[1];
        Lobby lobby = server.getLobby(lobbyId);
        if (lobby != null && lobby.addPlayer(ch)) {
            joinLobby(lobby);
            sendMessage("OK$JOIN$"+ lobbyId);
            return true;
        } else {
            sendMessage("ERR$106$JOIN_LOBBY_FAILED");
            return false;
        }
    }

    /**
     * This method handles a player (client) exiting a Lobby.
     */
    public boolean handleLeaveLobby() {
        if (currentLobby != null && currentLobby.removePlayer(ch)) {
            String lobbyId = currentLobby.getId();
            sendMessage("OK$LEAV$" + lobbyId);
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
     * This method handles the starting of a game.
     */
    public boolean handleStartGame() {
        if (currentLobby != null && currentLobby.getPlayers().get(0) == ch && currentLobby.startGame()) {
            this.gameLogic = currentLobby.getGameLogic();
            return true;
        } else {
            sendMessage("ERR$106$CANNOT_START_GAME");
            return false;
        }
    }

    /**
     * This method handles the registration of a player.
     * Gets called immediately when a player connects
     *
     * @param cmd the transmitted command
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
     */
    public boolean handleChangeName(Command cmd) {
        String newPlayerName = cmd.getArgs()[0].toLowerCase();

        synchronized (server) {
            if (!server.containsPlayerName(newPlayerName)) {
                server.broadcast("OK$CHAN$" + localPlayer.getName() + "$" + newPlayerName);
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
     * This method handles the sending of a private message to another player.
     *
     * @param cmd the transmitted command
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
                    sendMessage("CHTP$" + senderName + "$" + message);
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

    public boolean handleGlobalChatMessage(Command cmd) {
            ch.sendGlobalChatMessage(cmd);
            return true;
    }

    /**
     * Handles start turn command from client
     */
    public boolean handleStartTurn() {
        try {
            this.gameLogic.startTurn(localPlayer.getName());
            currentLobby.broadcastMessage("OK$TURN$" + playerName);
            return true;
        } catch (Exception e) {
            logger.warning("Could not start turn because game is not started yet.");
            return false;
        }
    }

    /**
     * Handles end turn command from client
     */
    public boolean handleEndTurn() {
        try {
            this.gameLogic.endTurn(localPlayer.getName());
            currentLobby.broadcastMessage("OK$ENDT$" + playerName);
            return true;
        } catch (Exception e) {
            logger.warning("Could not end turn because game is not started yet.");
            return false;
        }
    }

    /**
     * Handles synchronization request from client
     */
    public boolean handleSynchronize() {
        // TODO: Implement synchronization logic
        return false;
    }

    /**
     * Handles request for game status
     */
    public boolean handleGetGameStatus() {
        try {
            GameState gs = gameLogic.getGameState();
            sendMessage("OK$GSTS$" + gs.toString());
            return true;
        } catch (Exception e) {
            logger.severe("Failed to handle game status request: " + e.getMessage());
            sendMessage("ERR$106$GAME_STATUS_REQUEST_FAILED");
            return false;
        }
    }

    /**
     * Handles request for price information
     */
    public boolean handleGetPrices() {
        try {
            sendMessage("OK$GPRC$" + gameLogic.getPrices());
            return true;
        } catch (Exception e) {
            logger.severe("Failed to handle game status request: " + e.getMessage());
            sendMessage("ERR$106$GAME_STATUS_REQUEST_FAILED");
            return false;
        }
    }

    /**
     * Handles buy tile request from client
     * @param cmd the transmitted command
     */
    public boolean handleBuyTile(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length != 2) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$BUY_TILE");
                return false;
            }
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            if(gameLogic.buyTile(x, y, playerName)) {
                currentLobby.broadcastMessage("OK$BUYT$" + x + "$" + y +  "$" + playerName);
                return true;
            }
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates: " + e.getMessage());
            sendMessage("ERR$101$INVALID_COORDINATES");
        } catch (Exception e) {
            logger.severe("Failed to handle buy tile request: " + e.getMessage());
            sendMessage("ERR$106$BUY_TILE_FAILED");
        }
        return false;
    }

    /**
     * Handles buy structure request from client
     * @param cmd the transmitted command
     */
    public boolean handleBuyStructure(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length != 1) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$PLACE_STRUCTURE");
                return false;
            }
            String structureId = args[0];
            if(gameLogic.buyStructure(structureId, playerName)) {
                currentLobby.broadcastMessage("OK$BUST$" + structureId + "$playerName");
                return true;
            }
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates: " + e.getMessage());
            sendMessage("ERR$101$INVALID_COORDINATES");
        } catch (Exception e) {
            logger.severe("Failed to handle place structure request: " + e.getMessage());
            sendMessage("ERR$106$PLACE_STRUCTURE_FAILED");
        }
        return false;
    }

    /**
     * Handles place structure request from client
     * @param cmd the transmitted command
     */
    public boolean handlePlaceStructure(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length != 3) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$PLACE_STRUCTURE");
                return false;
            }
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            String structureId = args[2];
            if(gameLogic.placeStructure(x,y,structureId, playerName)) {
                currentLobby.broadcastMessage("OK$PLST$" + x + "$" + y + "$" + structureId + "$" + playerName);
                return true;
            }
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates: " + e.getMessage());
            sendMessage("ERR$101$INVALID_COORDINATES");
        } catch (Exception e) {
            logger.severe("Failed to handle place structure request: " + e.getMessage());
            sendMessage("ERR$106$PLACE_STRUCTURE_FAILED");
        }
        return false;
    }

    /**
     * Handles use structure request from client
     * @param cmd the transmitted command
     */
    public boolean handleUseStructure(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length != 4) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$USE_STRUCTURE");
                return false;
            }
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            String structureId = args[2];
            String useType = args[3];
            if(gameLogic.useStructure(x,y,structureId, useType, playerName)) {
                currentLobby.broadcastMessage("OK$USSR$" + x + "$" +  y + "$" + structureId + "$" + useType + "$" + playerName);
                return true;
            }
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates: " + e.getMessage());
            sendMessage("ERR$101$INVALID_COORDINATES");
        } catch (Exception e) {
            logger.severe("Failed to handle use structure request: " + e.getMessage());
            sendMessage("ERR$106$USE_STRUCTURE_FAILED");
        }
        return false;
    }

    /**
     * Handles buy statue request from client
     * @param cmd the transmitted command
     */
    public boolean handleBuyStatue(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length != 1) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$BUY_STATUE");
                return false;
            }
            String statueId = args[0];
            if (gameLogic.buyStatue(statueId, playerName)) {
                currentLobby.broadcastMessage("OK$BYST$" + statueId + "$playerName");
                return true;
            }
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates: " + e.getMessage());
            sendMessage("ERR$101$INVALID_COORDINATES");
        } catch (Exception e) {
            logger.severe("Failed to handle upgrade statue request: " + e.getMessage());
            sendMessage("ERR$106$BUY_STATUE_FAILED");
        }
        return false;
    }

    /**
     * Handles upgrade statue request from client
     * @param cmd the transmitted command
     */
    public boolean handleUpgradeStatue(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length != 3) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$UPGRADE_STATUE");
                return false;
            }
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            String statueId = args[2];
            if (gameLogic.upgradeStatue(x,y,statueId, playerName)) {
                currentLobby.broadcastMessage("OK$UPST$" + x + "$" + y + "$" + statueId + "$" + playerName);
                return true;
            }
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates: " + e.getMessage());
            sendMessage("ERR$101$INVALID_COORDINATES");
        } catch (Exception e) {
            logger.severe("Failed to handle upgrade statue request: " + e.getMessage());
            sendMessage("ERR$106$UPGRADE_STATUE_FAILED");
        }
        return false;
    }

    /**
     * Handles use statue request from client
     * @param cmd the transmitted command
     */
    public boolean handleUseStatue(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length != 4) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$USE_STATUE");
                return false;
            }
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            String statueId = args[2];
            String useType = args[3];
            if (gameLogic.useStatue(x,y,statueId, useType, playerName)) {
                currentLobby.broadcastMessage("OK$USTA$" + x + "$" + y + "$" + statueId + "$" + useType + "$" + playerName);
                return true;
            }
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates: " + e.getMessage());
            sendMessage("ERR$101$INVALID_COORDINATES");
        } catch (Exception e) {
            logger.severe("Failed to handle use statue request: " + e.getMessage());
            sendMessage("ERR$106$USE_STATUE_FAILED");
        }
        return false;
    }

    /**
     * Handles use player artifact request from client
     * @param cmd the transmitted command
     */
    public boolean handleUsePlayerArtifact(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length != 2) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$USE_PLAYER_ARTIFACT");
                return false;
            }
            int artifactId = Integer.parseInt(args[0]);
            String useType = args[1];
            String playerAimedAt = args[2];
            if(gameLogic.usePlayerArtifact(artifactId,playerName, useType, playerAimedAt)) {
                currentLobby.broadcastMessage("OK$USPA$" + artifactId + "$" + playerName + "$" + useType);
            }
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse artifact ID: " + e.getMessage());
            sendMessage("ERR$101$INVALID_ARTIFACT_ID");
        } catch (Exception e) {
            logger.severe("Failed to handle use player artifact request: " + e.getMessage());
            sendMessage("ERR$106$USE_PLAYER_ARTIFACT_FAILED");
        }
        return false;
    }

    /**
     * Handles use player artifact request from client
     * @param cmd the transmitted command
     */
    public boolean handleUseFieldArtifact(Command cmd) {
        try {
            String[] args = cmd.getArgs();
            if (args.length != 4) {
                sendMessage("ERR$101$INVALID_ARGUMENTS$USE_FIELD_ARTIFACT");
                return false;
            }
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int artifactId = Integer.parseInt(args[2]);
            String useType = args[3];
            if (gameLogic.useFieldArtifact(x,y,artifactId,useType)) {
                currentLobby.broadcastMessage("OK$USFA$" + x + "$" + y + artifactId + "$" + playerName + "$" + useType);
                return true;
            }
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse artifact ID: " + e.getMessage());
            sendMessage("ERR$101$INVALID_ARTIFACT_ID");
        } catch (Exception e) {
            logger.severe("Failed to handle use player artifact request: " + e.getMessage());
            sendMessage("ERR$106$USE_PLAYER_ARTIFACT_FAILED");
        }
        return false;
    }
}
