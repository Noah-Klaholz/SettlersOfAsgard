package ch.unibas.dmi.dbis.cs108.server.command;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;
import ch.unibas.dmi.dbis.cs108.server.util.Logger;
import ch.unibas.dmi.dbis.cs108.shared.protocol.ErrorsAPI;

/**
 * Command to register on the server with a username.
 * Usage: login <username>
 */
public class RegisterCommand implements Command {

    public RegisterCommand() {
        // No auth service needed
    }

    @Override
    public void execute(String[] args, ClientHandler client) {
        if (args.length != 2) {
            client.sendMessage(ErrorsAPI.Errors.NOT_CORRECT_ARGUMENTS.getError());
            return;
        }

        String username = args[1];
        client.sendMessage("Welcome to the game, " + username + "!");
        Logger.info("Client joined as " + username);
    }

    @Override
    public String getName() {
        return "login";
    }
}