package ch.unibas.dmi.dbis.cs108.client.ui.events;

import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.NetworkProtocol.*;

/**
 * Event representing a command received from the server.
 * Uses the shared CommunicationAPI to define the command types.
 */
public class ReceiveCommandEvent {
    private final String message;
    private final Commands commandType;

    // Commands have OK added in front of them, but this is removed upon event creation
    public ReceiveCommandEvent(String message) {
        this.message = message.replaceAll("OK\\$", "").trim().toLowerCase();
        this.commandType = Commands.fromCommand(message.split("\\$")[0]);
    }

    public String getMessage() {
        return message;
    }

    public Commands getType() {
        return commandType;
    }

}