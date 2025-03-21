package ch.unibas.dmi.dbis.cs108.client.core.commands;

/**
 * Command interface is responsible for executing commands
 */
public interface Command {
    /**
     * Executes the command
     *
     * @return String
     */
    void execute();
}
