package ch.unibas.dmi.dbis.cs108.client.ui.events;

import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.NetworkProtocol.Commands;

public class SendCommandEvent {
    private final String message;
    private final Commands commandType;

    public SendCommandEvent(String message) {
        this.message = message;
        this.commandType = Commands.fromCommand(message);
    }
    public String getMessage() {
        return message;
    }

    public Commands getType() {
        return commandType;
    }

}