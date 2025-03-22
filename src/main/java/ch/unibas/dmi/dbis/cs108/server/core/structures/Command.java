package ch.unibas.dmi.dbis.cs108.server.core.structures;

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
     * Correct formatting: commandName$arg1$arg2$arg3
     */
    public Command(String message) {
        String[] parts = message.split("\\$", 2);
        if(parts.length != 2) {
            System.err.println("Invalid command: " + message);
            return;
        }
        this.command = parts[0];
        this.args = Arrays.copyOfRange(parts, 1, parts.length);;
    }

    /**
     * Checks if the command is valid
     * @return true if the command is valid, false otherwise
     */
    public boolean isValid() {
        // Check that the command is not null
        if (command == null) {
            return false;
        }
        // Special cases: OK, TEST and ERR (always valid)
        if (command.equals("OK") || command.equals("ERR") || command.equals("TEST")) {
            return true;
        }
        // Validate command length (must be exactly 4 characters)
        if (command.length() != 4) {
            return false;
        }
        // Check arguments for each command individually
        return checkArgumentsSize();
        // Later: add argument checking for game logic commands

    }

    public boolean checkArgumentsSize(){
        return switch (command) {
            case "LIST", "STRT", "STDN", "PING", "SYNC" -> args.length == 0;
            case "RGST", "CHAN", "STAT" -> args.length == 1;
            case "JOIN", "EXIT", "CHTG" -> args.length == 2;
            case "CHTP" -> args.length == 3;
            default -> false;
        };
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
        String args = String.join("$", this.args);
        return command + "$" + args;
    }
}
