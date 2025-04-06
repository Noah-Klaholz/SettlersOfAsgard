package ch.unibas.dmi.dbis.cs108.client.ui.events;

import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.NetworkProtocol.Commands;

public class SendCommandEvent {
    private final String message;
    private final CommandType commandType;

    public SendCommandEvent(String message) {
        this.message = message;
        this.commandType = CommandType.fromCommand(message.split(" ")[0]);
    }

    public String getMessage() {
        return message;
    }

    public CommandType getType() {
        return commandType;
    }

    public enum CommandType {
        // All / Commands (except ping, because user should not be able to send it)
        EXIT("/exit"),
        CHANGENAME("/changename"),
        JOINLOBBY("/join"),
        LEAVELOBBY("/leave"),
        CREATELOBBY("/create"),
        STARTGAME("/start"),
        LISTLOBBIES("/listlobbies"),
        GLOBALCHAT("/global"),
        HELP("/help"),
        BUYTILE("/buytile"),
        PLACESTRUCTURE("/placestructure"),
        USESTRUCTURE("/usestructure"),
        UPGRDESTATUE("/upgradestructure"),
        USESTATUE("/usestatue"),
        USEFIELDARTIFACT("/usefieldartifact"),
        USEPLAYERARTIFACT("/useplayerartifact"),
        STATUS("/status"),
        PRICES("/prices");

        private final String command;

        CommandType(String command) {
            this.command = command;
        }

        public static CommandType fromCommand(String command) {
            for (CommandType type : values()) {
                if (type.command.equalsIgnoreCase(command)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown SendCommand-Event command: " + command);
        }
    }

}