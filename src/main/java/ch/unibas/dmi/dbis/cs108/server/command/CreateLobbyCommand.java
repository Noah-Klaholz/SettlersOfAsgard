package ch.unibas.dmi.dbis.cs108.server.command;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.core.lobby.Lobby;
import ch.unibas.dmi.dbis.cs108.server.core.lobby.LobbyManager;

public class CreateLobbyCommand implements Command {
    private final LobbyManager lobbyManager;

    public CreateLobbyCommand(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @Override
    public void execute(String[] args, ClientHandler client) {
        if (args.length < 2) {
            client.sendMessage("Usage: create_lobby <lobby_name>");
            return;
        }

        String lobbyName = args[1];
        Lobby lobby = lobbyManager.createLobby(lobbyName);
        client.sendMessage("Lobby created: " + lobbyName);
    }

    @Override
    public String getName() {
        return "create_lobby";
    }
}
