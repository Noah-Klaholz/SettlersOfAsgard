package ch.unibas.dmi.dbis.cs108.server.command;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;

public class PingCommand implements Command {
    @Override
    public void execute(String[] args, ClientHandler client) {
        // Parse command properly if it contains "$"
        String playerName = "";
        if (args.length > 0 && args[0].contains("$")) {
            String[] parts = args[0].split("\\$", 2);
            if (parts.length > 1) {
                playerName = parts[1];
            }
        } else if (args.length > 1) {
            playerName = args[1];
        }

        // Send the expected ok$ping response format
        client.sendMessage("OK$PING");
    }

    @Override
    public String getName() {
        return "ping";
    }
}