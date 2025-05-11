package ch.unibas.dmi.dbis.cs108.client.ui.events.admin;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

import java.util.List;

/**
 * UIEvent representing a command received from the server that may require a UI
 * action.
 */
public class ServerCommandEvent implements UIEvent {

    /**
     * The command type.
     */
    private final String command;
    /**
     * The command arguments.
     */
    private final List<String> arguments;
    /**
     * An optional descriptive message.
     */
    private final String message;

    /**
     * Constructs a new ServerCommandEvent.
     *
     * @param command   the command type
     * @param arguments the command arguments
     * @param message   an optional descriptive message
     */
    public ServerCommandEvent(String command, List<String> arguments, String message) {
        this.command = command;
        this.arguments = arguments;
        this.message = message;
    }

    /**
     * Gets the command type.
     *
     * @return the command type
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets the command arguments.
     *
     * @return the command arguments
     */
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * Gets the descriptive message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "SERVER_COMMAND";
    }
}
