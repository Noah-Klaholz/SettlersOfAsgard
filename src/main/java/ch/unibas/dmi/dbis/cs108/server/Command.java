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
     * @param message the String message
     * Prints out error Message in case of wrong formatting of message
     * Correct formatting: commandName:arg1,arg2,arg3
     */
    public Command(String message) {
        String[] parts = message.split(":", 2);
        if(parts.length != 2) {
            System.err.println("Invalid command: " + message);
            return;
        }
        this.command = parts[0];
        this.args = parts[1].split(",");
    }

    /**
     * Checks if the command is valid
     * @return true if the command is valid, false otherwise
     */
    public boolean validCommand() {
        // TODO: Implement checking for correct commandName and valid arguments (Netzwerkprotokoll vorher festlegen)
        // Should check for correct command and arguments
        return command != null && args != null;
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
