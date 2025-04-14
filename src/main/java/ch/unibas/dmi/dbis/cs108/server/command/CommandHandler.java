package ch.unibas.dmi.dbis.cs108.server.command;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Maps command names to their implementations.
 * Parses input from clients and dispatches to the proper command.
 */
public class CommandHandler {
    private final Map<String, Command> commandMap;

    public CommandHandler() {
        commandMap = new HashMap<>();
    }

    /**
     * Registers a command.
     *
     * @param command The command to register.
     */
    public void registerCommand(Command command) {
        commandMap.put(command.getName(), command);
    }

    /**
     * Executes the command given by input.
     *
     * @param input  Raw client input.
     * @param client The client who sent the command.
     */
    public void executeCommand(String input, ClientHandler client) {
        String[] tokens = input.trim().split("\\$");
        if (tokens.length == 0) return;
        String commandName = tokens[0];
        Command command = commandMap.get(commandName);
        if (command != null) {
            command.execute(tokens, client);
        } else {
            client.sendMessage("Unknown command: " + commandName);
        }
    }

    /**
     * Returns the set of registered command names.
     */
    public Set<String> getRegisteredCommands() {
        return commandMap.keySet();
    }
}