package ch.unibas.dmi.dbis.cs108.client.ui.events;

public class ReceiveCommandEvent {
    private final String message;
    private final CommandType commandType;

    public ReceiveCommandEvent(String message) {
        this.message = message.replaceAll("OK\\$", "").trim().toLowerCase();
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
        LISTALLPLAYERS("/listallplayers"),
        LISTLOBBYPLAYERS("/listlobbyplayers"),
        GLOBALCHAT("/global"),
        HELP("/help"),
        BUYTILE("/buytile"),
        PLACESTRUCTURE("/placestructure"),
        USESTRUCTURE("/usestructure"),
        UPGRADESTATUE("/upgradestructure"),
        USESTATUE("/usestatue"),
        USEFIELDARTIFACT("/usefieldartifact"),
        USEPLAYERARTIFACT("/useplayerartifact"),
        STATUS("/status"),
        PRICES("/prices");

        private final String command;

        CommandType(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
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