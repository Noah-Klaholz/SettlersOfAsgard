package ch.unibas.dmi.dbis.cs108.server.core.structures;

import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.protocol.ErrorsAPI;

import java.util.Arrays;
import java.util.logging.Logger;

import static ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.NetworkProtocol.Commands;
import static ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.PingFilter;

/**
 * Represents a command that is sent between a client to the server
 */
public class Command {
    /**
     * Logger for the Command class
     */
    private static final Logger logger = Logger.getLogger(Command.class.getName());
    /**
     * The command that is sent
     */
    private String command;
    /**
     * The command type
     */
    private Commands commandType;
    /**
     * The arguments that are sent with the command
     */
    private String[] args;
    /**
     * The player who sent the command
     */
    private Player player;

    /**
     * Creates a new command
     *
     * @param message the String message
     *                Prints out error Message in case of wrong formatting of message
     *                Correct formatting: commandName$arg1$arg2$arg3
     * @param player  the player who sent the command
     */
    public Command(String message, Player player) {
        logger.setFilter(new PingFilter());
        if (message == null || message.trim().isEmpty()) {
            logger.warning(ErrorsAPI.Errors.NULL_MESSAGE_RECIEVED.getError());
        } else {
            String[] parts = message.split("[$]");
            if (parts.length < 1) {
                logger.warning(ErrorsAPI.Errors.INVALID_COMMAND.getError());
            }

            this.command = parts[0];
            this.player = player;
            try {
                this.commandType = Commands.fromCommand(command);
            } catch (IllegalArgumentException e) {
                logger.warning(ErrorsAPI.Errors.UNKNOWN_COMMAND.getError() + " : " + command);
            }
            this.args = Arrays.copyOfRange(parts, 1, parts.length);
        }
    }

    /**
     * Checks if the command is valid
     *
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
    }

    /**
     * Checks whether the command has the correct number of arguments appended
     *
     * @return true if the command has the correct number of arguments, false otherwise
     */
    public boolean checkArgumentsSize() {
        return switch (commandType) {
            case LISTLOBBIES, START, SHUTDOWN, SYNCHRONIZE, STARTTURN, ENDTURN, GETGAMESTATUS, GETPRICES, LEADERBOARD ->
                    args.length == 0;
            case REGISTER, LEAVE, CHANGENAME, PING, EXIT, DISCONNECT, CHEAT, RECONNECT -> args.length == 1;
            case JOIN, CHATGLOBAL, CHATLOBBY, BUYTILE, USEPLAYERARTIFACT -> args.length == 2;
            case CHATPRIVATE, PLACESTRUCTURE, PLACESTATUE, UPGRADESTATUE, CREATELOBBY, USESTRUCTURE, USEFIELDARTIFACT ->
                    args.length == 3;
            case USESTATUE -> args.length == 4;
            case LISTPLAYERS ->
                    (args.length == 1 && args[0].equals("SERVER")) || (args.length == 2 && args[0].equals("LOBBY"));
            default -> {
                logger.warning("Invalid Command arguments size: " + command + " " + args.length);
                yield false;
            }
        };
    }

    /**
     * Gets the command
     *
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets the command type
     *
     * @return the command type
     */
    public Commands getCommandType() {
        return commandType;
    }

    /**
     * Gets the arguments
     *
     * @return the arguments as a String Array
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * Gets the player
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Converts the command and the arguments to a String
     *
     * @return the command and the arguments as a String
     */
    @Override
    public String toString() {
        String args = String.join("$", this.args);
        return command + "$" + args;
    }

    /**
     * Checks if the command is administrative
     *
     * @return true if the command is administrative, false otherwise
     */
    public boolean isAdministrative() {
        return switch (commandType) {
            case LEADERBOARD, LISTLOBBIES, START, SHUTDOWN, SYNCHRONIZE, REGISTER, LEAVE, CHANGENAME, PING, EXIT, JOIN,
                 CHATGLOBAL, CHATLOBBY, CHATPRIVATE, CREATELOBBY, LISTPLAYERS, OK, DISCONNECT, RECONNECT -> true;
            case CHEAT, GETGAMESTATUS, GETPRICES, STARTTURN, ENDTURN, BUYTILE, PLACESTRUCTURE, USEPLAYERARTIFACT,
                 PLACESTATUE, UPGRADESTATUE, USESTATUE, USESTRUCTURE, USEFIELDARTIFACT -> false;
            default -> {
                logger.warning("Invalid Command " + command + " " + Arrays.toString(args));
                yield false;
            }
        };
    }

    /**
     * Helper method to format error responses
     */
    private String formatError(String message) {
        return Commands.ERROR.getCommand() + ":" + message;
    }
}
