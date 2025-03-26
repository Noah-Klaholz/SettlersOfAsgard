package ch.unibas.dmi.dbis.cs108.server.core.structures.protocol;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.networking.GameServer;

import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHandler {
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

    public void handleListPlayers(Command cmd) {
        String[] arg = cmd.getArgs();
        if(arg.length != 1) {
            sendMessage("ERR$101$INVALID_ARGUMENTS");
        } else {
            String list;
            if(arg[0].equals("LOBBY")) {
                if(currentLobby != null) {
                    list = currentLobby.listPlayers();
                    sendMessage(list);
                } else {
                    sendMessage("ERR$106$NOT_IN_LOBBY");
                }
            } else if(arg[0].equals("SERVER")) {
                list = server.listPlayers();
                sendMessage(list);
            } else {
                sendMessage("ERR$101$INVALID_ARGUMENTS");
            }

        }
    }

    /**
     * This method handles the creation of a lobby.
     * @param cmd the transmitted command
     */
    public void handleCreateLobby(Command cmd) {
        String hostname = cmd.getArgs()[0]; // Falls wir später mal den Hostnamen speichern wollen -> könnte man in Lobby hinzufügen
        String lobbyId = cmd.getArgs()[1];
        int maxPlayers = 4; //currently, maxPlayers is set to 4
        Lobby lobby = server.createLobby(lobbyId, maxPlayers);
        if (lobby != null && lobby.addPlayer(ch)) {
            setCurrentLobby(lobby);
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
                .map(Lobby::getId)
                .collect(Collectors.joining(", "));

        ch.sendMessage("Lobbies: " + lobbyList);
    }

    /**
     * This method handles a player (client) joining a Lobby.
     * @param cmd the transmitted command
     */
    public void handleJoinLobby(Command cmd) {
        String lobbyId = cmd.getArgs()[1];
        Lobby lobby = server.getLobby(lobbyId);
        if (lobby != null && lobby.addPlayer(ch)) {
            setCurrentLobby(lobby);
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
            if(lobby != null && lobby.isEmpty()) {
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
     * @param cmd the transmitted command
     */
    public void handleRegister(Command cmd) {
        String playerName = cmd.getArgs()[0];
        if (!server.containsPlayerName(playerName)) {
            setLocalPlayer(new Player(playerName));
            sendMessage("OK$RGST$" + playerName);
        }
        else {
            playerName = playerName + "2";
            setLocalPlayer(new Player(playerName)); // Adds playerName2 as a new Player
            sendMessage("ERR$106$PLAYER_ALREADY_EXISTS$"+playerName); // Tells Client to tell player about changeName
        }
    }

    /**
     * This method handles the changing of a player's name.
     * @param cmd the transmitted command
     */
    public void handleChangeName(Command cmd) {
        String newPlayerName = cmd.getArgs()[0];
        if (!server.containsPlayerName(newPlayerName)){
            sendMessage("OK$CHAN$" + newPlayerName);
            server.broadcast(localPlayer.getName() + " changed name to " + newPlayerName);
            setLocalPlayerName(newPlayerName);
        }
        else {
            newPlayerName = newPlayerName + "2";
            setLocalPlayerName(newPlayerName); // Change the name to the enw name
            sendMessage("ERR$106$PLAYER_ALREADY_EXISTS$"+newPlayerName); // Tells Client to tell player about changeName
        }
    }

    /**
     * This method handles the sending of a private message to another player.
     * @param cmd the transmitted command
     */
    public void handlePrivateMessage(Command cmd) {
        String[] parts = cmd.getArgs();
        String senderName = parts[0];
        String receiverName = parts[1];
        String message = parts[2];
        if(server.containsPlayerName(receiverName)) {
        }
    }
}
