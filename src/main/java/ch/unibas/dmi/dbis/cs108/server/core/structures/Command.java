package ch.unibas.dmi.dbis.cs108.server.core.structures;

import java.util.Arrays;
import java.util.logging.Logger;

import static ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.PingFilter;

/**
 * Represents a command that is sent between a client to the server
 */
public class Command {
    private static final Logger logger = Logger.getLogger(Command.class.getName());
    private String command;
    private String[] args;

    /**
     * Creates a new command
     * @param message the String message
     * Prints out error Message in case of wrong formatting of message
     * Correct formatting: commandName$arg1$arg2$arg3
     */
    public Command(String message) {
        logger.setFilter(new PingFilter());
        String[] parts = message.split("\\$");
        if(parts.length == 0){
            System.err.println("Trying to create invalid command: " + message);
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
            logger.warning("Invalid Command: Cannot be null");
            return false;
        }
        // Special cases: OK, TEST and ERR (always valid)
        if (command.equals("OK") || command.equals("ERR") || command.equals("TEST")) {
            return true;
        }
        // Validate command length (must be exactly 4 characters)
        if (command.length() != 4) {
            logger.warning("Command length is not 4 characters: " + command);
            return false;
        }
        // Check arguments for each command individually
        return checkArgumentsSize();
        // Later: add argument checking for game logic commands

    }

    /**
     * Checks whether the command has the correct number of arguments appended
     * @return
     */
    public boolean checkArgumentsSize(){
        return switch (command) {
            case "LIST", "STRT", "STDN", "SYNC" -> args.length == 0;
            case "RGST", "CHAN", "STAT", "PING", "EXIT", "LSTP"  -> args.length == 1;
            case "JOIN", "LEAV", "CHTG", "CREA" -> args.length == 2;
            case "CHTP" -> args.length == 3;
            default -> {
                logger.warning("Invalid Command arguments size: " + command + " " + args.length);
                yield false;
            }
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
