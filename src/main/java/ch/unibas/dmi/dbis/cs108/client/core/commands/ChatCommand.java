package ch.unibas.dmi.dbis.cs108.client.core.commands;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;

public class ChatCommand implements GameCommand {
    private final Player sender;
    private final String message;

    public ChatCommand(Player sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Player getSender() {
        return sender;
    }

    @Override
    public void execute() {
        // Only handle local effects like:
        // - Updating the local chat UI
        // - Adding to chat history
        System.out.println("Local: " + sender.getName() + ": " + message);
    }
}
