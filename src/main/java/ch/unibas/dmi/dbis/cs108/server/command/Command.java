package ch.unibas.dmi.dbis.cs108.server.command;

import ch.unibas.dmi.dbis.cs108.server.core.ClientHandler;

/**
 * Interface for all commands.
 */
public interface Command {
    /**
     * Executes the command.
     *
     * @param args   The command arguments (first element is the command name).
     * @param client The client issuing the command.
     */
    void execute(String[] args, ClientHandler client);

    /**
     * Returns the unique name of the command.
     *
     * @return The command name.
     */
    String getName();
}
