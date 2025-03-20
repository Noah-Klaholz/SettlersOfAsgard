package ch.unibas.dmi.dbis.cs108.client.core.commands;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;

/**
 * ChatCommand class is responsible for creating a chat command
 */
public class ChatCommand implements GameCommand {
    private final Player sender;
    private final String message;

    /**
     * Constructor for ChatCommand class
     *
     * @param sender  Player
     * @param message String
     */
    public ChatCommand(Player sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    /**
     * Getter for message
     *
     * @return String
     */
    public String getMessage() {
        return message;
    }

    /**
     * Getter for sender
     *
     * @return Player
     */
    public Player getSender() {
        return sender;
    }

    /**
     * Executes the command
     */
    @Override
    public void execute() {
        // Only handle local effects like:
        // - Updating the local chat UI
        // - Adding to chat history
        System.out.println("Local: " + sender.getName() + ": " + message);
    }
}
