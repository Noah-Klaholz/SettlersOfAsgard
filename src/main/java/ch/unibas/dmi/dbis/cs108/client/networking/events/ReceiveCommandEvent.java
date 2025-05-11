package ch.unibas.dmi.dbis.cs108.client.networking.events;

import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.NetworkProtocol.Commands;

import java.time.Instant;

/**
 * UIEvent representing a command received from the server.
 * Uses the shared CommunicationAPI to define the command types.
 */
public class ReceiveCommandEvent implements Event {
    /**
     * The timestamp of the event.
     */
    private final Instant timestamp = Instant.now();
    /**
     * The message received from the server.
     */
    private final String message;
    /**
     * The type of command received.
     */
    private final Commands commandType;

    /**
     * Constructor for ReceiveCommandEvent.
     *
     * @param message The message received from the server.
     */
    // Commands have OK added in front of them, but this is removed upon event
    // creation
    public ReceiveCommandEvent(String message) {
        this.message = message.replaceAll("OK\\$", "").trim();
        this.commandType = Commands.fromCommand(message.split("\\$")[0]);
    }

    /**
     * Constructor for ReceiveCommandEvent without OK prefix.
     *
     * @param message     The message received from the server.
     * @param commandType The type of command.
     */
    // Constructor for commands without OK prefix
    public ReceiveCommandEvent(String message, Commands commandType) {
        this.message = message;
        this.commandType = commandType;
    }

    /**
     * Get the timestamp of the event.
     *
     * @return The timestamp.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the command type of the event.
     *
     * @return The command type.
     */
    public Commands getType() {
        return commandType;
    }

    /**
     * Getter for the timestamp of the event.
     *
     * @return The timestamp of the event.
     */
    @Override
    public Instant getTimestamp() {
        return null;
    }
}
