package ch.unibas.dmi.dbis.cs108.client.core.commands.chat;

import ch.unibas.dmi.dbis.cs108.client.core.commands.Command;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;

/**
 * CreateLobbyCommand class is responsible for creating a chat command
 * This command is used to create a lobby
 */
public class CreateLobbyCommand implements Command {
    private final Player sender;
    private final String lobbyId;

    /**
     * Constructor for CreateLobbyCommand class
     *
     * @param sender  Player
     * @param lobbyId String
     */
    public CreateLobbyCommand(Player sender, String lobbyId) {
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
        System.out.println(sender.getName() + " created Lobby: " + lobbyId);
    }
}
