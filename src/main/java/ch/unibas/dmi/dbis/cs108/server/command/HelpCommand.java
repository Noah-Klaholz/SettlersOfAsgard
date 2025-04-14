package ch.unibas.dmi.dbis.cs108.server.command;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;

/**
 * Command that lists all available commands.
 */
public class HelpCommand implements Command {

    private final CommandHandler commandHandler;

    public HelpCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void execute(String[] args, ClientHandler client) {
        client.sendMessage("Available commands:");
        for (String cmd : commandHandler.getRegisteredCommands()) {
            client.sendMessage("- " + cmd);
        }
    }

    @Override
    public String getName() {
        return "help";
    }
}