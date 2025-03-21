package core.commands;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;

public class PongCommand implements Command {
    private final Player sender;
    private final String message;

    public PongCommand(Player sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public Player getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void execute() {
        System.out.println("Local: " + sender.getName() + " is ponging the server with message: " + message);
    }
}
