package ch.unibas.dmi.dbis.cs108.server.command;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.core.lobby.Lobby;
import ch.unibas.dmi.dbis.cs108.server.core.lobby.LobbyManager;

import java.util.Collection;

public class ListLobbiesCommand implements Command {
    private final LobbyManager lobbyManager;

    public ListLobbiesCommand(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @Override
    public void execute(String[] args, ClientHandler client) {
        Collection<Lobby> lobbies = lobbyManager.getAllLobbies();
        StringBuilder response = new StringBuilder("Available lobbies:\n");

        if (lobbies.isEmpty()) {
            response.append("No lobbies available");
        } else {
            for (Lobby lobby : lobbies) {
                response.append("- ").append(lobby.getLobbyId()).append("\n");
            }
        }

        client.sendMessage(response.toString().trim());
    }

    @Override
    public String getName() {
        return "list_lobbies";
    }
}
