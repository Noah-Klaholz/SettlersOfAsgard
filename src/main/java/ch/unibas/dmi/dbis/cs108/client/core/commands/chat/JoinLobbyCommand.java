//package ch.unibas.dmi.dbis.cs108.client.core.commands.chat;
//
//import ch.unibas.dmi.dbis.cs108.client.core.commands.Command;
//import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
//
///**
// * JoinLobbyCommand class is responsible for creating a chat command
// * This command is used to join a lobby
// */
//public class JoinLobbyCommand implements Command {
//    private final Player sender;
//    private final String lobbyId;
//
//    /**
//     * Constructor for JoinLobbyCommand class
//     *
//     * @param sender  Player
//     * @param lobbyId String
//     */
//    public JoinLobbyCommand(Player sender, String lobbyId) {
//        this.sender = sender;
//        this.lobbyId = lobbyId;
//    }
//
//    /**
//     * Getter for message
//     *
//     * @return String
//     */
//    public String getLobbyId() {
//        return lobbyId;
//    }
//
//    /**
//     * Getter for sender
//     *
//     * @return Player
//     */
//    public Player getSender() {
//        return sender;
//    }
//
//    /**
//     * Executes the command
//     */
//    @Override
//    public void execute() {
//        // Only handle local effects like:
//        // - Updating the local chat UI
//        // - Adding to chat history
//        System.out.println(sender.getName() + " joined Lobby: " + lobbyId);
//    }
//}
