package ch.unibas.dmi.dbis.cs108.client.core.commands;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;

public class ChatCommand implements GameCommand {
    private final Player sender;
    private final String message;

    public ChatCommand(Player sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public Player getSender() {
        return null;
    }

    public String getMessage() {
        return null;
    }

    @Override
    public String execute() {
        return null;
    }
}
