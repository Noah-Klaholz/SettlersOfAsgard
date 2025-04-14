package ch.unibas.dmi.dbis.cs108.server.command;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.core.logic.GameCommunicationAdapter;

public class GameCommandAdapter implements Command {
    private final GameCommunicationAdapter communicationAdapter;

    public GameCommandAdapter(GameCommunicationAdapter communicationAdapter) {
        this.communicationAdapter = communicationAdapter;
    }

    @Override
    public void execute(String[] args, ClientHandler client) {
        communicationAdapter.addClient(client);

        // Convert command format: game command arg1 arg2 -> COMMAND$arg1,arg2
        if (args.length < 2) {
            client.sendMessage("Usage: game <command> [arguments]");
            return;
        }

        String commandName = args[1].toUpperCase();
        StringBuilder messageBuilder = new StringBuilder(commandName);

        if (args.length > 2) {
            messageBuilder.append("$");
            for (int i = 2; i < args.length; i++) {
                messageBuilder.append(args[i]);
                if (i < args.length - 1) {
                    messageBuilder.append(",");
                }
            }
        }

        communicationAdapter.handleClientMessage(messageBuilder.toString(), client);
    }

    @Override
    public String getName() {
        return "game";
    }
}