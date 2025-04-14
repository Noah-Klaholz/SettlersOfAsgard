package ch.unibas.dmi.dbis.cs108.server.command;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.core.lobby.LobbyManager;

public class ChatGlobalCommand implements Command {
    private final LobbyManager lobbyManager;

    public ChatGlobalCommand(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @Override
    public void execute(String[] args, ClientHandler client) {
        if (args.length < 2) {
            client.sendMessage("Usage: chatglobal <message>");
            return;
        }

        // Reconstruct the message from args
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }
        String message = messageBuilder.toString();

        // Broadcast to all clients across all lobbies
        // Todo: add sender name to message
        lobbyManager.broadcastToAllClients("CHATGLOBAL:" + ":" + message);
    }

    @Override
    public String getName() {
        return "chatglobal";
    }
}