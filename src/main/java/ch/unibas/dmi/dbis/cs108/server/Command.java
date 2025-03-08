package ch.unibas.dmi.dbis.cs108.server;

import java.util.Arrays;

/**
 * Represents a command that is sent between a client to the server
 */
public class Command {
    private String command;
    private String[] args;

    /**
     * Creates a new command
     * @param command the command
     * @param args the arguments
     */
    public Command(String command, String[] args) {
        this.command = command;
        this.args = args;
    }

    /**
     * Gets the command
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets the arguments
     * @return the arguments as a String Array
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * Converts the command and the arguments to a String
     * @return the command and the arguments as a String
     */
    @Override
    public String toString() {
        return "Command:" + command + ", args=" + Arrays.toString(args);
    }
}
