package ch.unibas.dmi.dbis.cs108.client.core.commands;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;

/**
 * LeaveLobbyCommand class is responsible for handling lobby exit operations
 */
public class LeaveLobbyCommand implements Command {
    private final Player sender;
    private final String lobbyId;

    /**
     * Constructor for ChatCommand class
     *
     * @param sender  Player
     * @param lobbyId String
     */
    public LeaveLobbyCommand(Player sender, String lobbyId) {
        this.sender = sender;
        this.lobbyId = lobbyId;
    }

    /**
     * Getter for message
     *
     * @return String
     */
    public String getLobbyId() {
        return lobbyId;
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
        System.out.println(sender.getName() + " left Lobby: " + lobbyId);
    }
}
