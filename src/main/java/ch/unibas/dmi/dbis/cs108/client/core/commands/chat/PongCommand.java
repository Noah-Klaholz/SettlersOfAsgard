//package ch.unibas.dmi.dbis.cs108.client.core.commands.chat;
//
//import ch.unibas.dmi.dbis.cs108.client.core.commands.Command;
//import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
//
///**
// * The PongCommand is used to respond to a ping from the server.
// * It is used to measure the latency between the client and the server.
// * The server will send a ping command to the client, and the client will respond with a pong command.
// */
//public class PongCommand implements Command {
//    private final Player sender;
//    private final String targetId;
//
//    /**
//     * Constructor for the PongCommand.
//     *
//     * @param sender
//     * @param targetId
//     */
//    public PongCommand(Player sender, String targetId) {
//        this.sender = sender;
//        this.targetId = targetId;
//    }
//
//    /**
//     * Getter for the sender of the pong command.
//     *
//     * @return
//     */
//    public Player getSender() {
//        return sender;
//    }
//
//    /**
//     * Getter for the targetId of the pong command.
//     *
//     * @return
//     */
//    public String getTargetId() {
//        return targetId;
//    }
//
//    /**
//     * Method to execute the pong command.
//     * Prints playerName who is ponging the server
//     */
//    @Override
//    public void execute() {
//        System.out.println("Local: " + sender.getName() + " is ponging the server with message: " + targetId);
//    }
//}
