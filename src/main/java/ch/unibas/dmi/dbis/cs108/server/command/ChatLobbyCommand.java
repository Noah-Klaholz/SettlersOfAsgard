package ch.unibas.dmi.dbis.cs108.server.command;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.core.lobby.Lobby;
import ch.unibas.dmi.dbis.cs108.server.core.lobby.LobbyManager;

public class ChatLobbyCommand implements Command {
    private final LobbyManager lobbyManager;

    public ChatLobbyCommand(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @Override
    public void execute(String[] args, ClientHandler client) {
        if (args.length < 2) {
            client.sendMessage("Usage: chatlobby <message>");
            return;
        }

        // Reconstruct the message from args
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }
        String message = messageBuilder.toString();

        // Get client's current lobby and broadcast only to that lobby
        Lobby clientLobby = lobbyManager.getClientLobby(client);
        if (clientLobby == null) {
            client.sendMessage("Error: You are not in any lobby");
            return;
        }

        // Todo: add sender name to message
        clientLobby.broadcastMessage("CHATLOBBY:" + ":" + message);
    }

    @Override
    public String getName() {
        return "chatlobby";
    }
}