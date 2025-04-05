//package ch.unibas.dmi.dbis.cs108.client.core.commands.chat;
//
//import ch.unibas.dmi.dbis.cs108.client.core.commands.Command;
//import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
//
///**
// * PingCommand class is responsible for creating a chat command
// * This command is used to ping the server
// */
//public class PingCommand implements Command {
//    private final Player sender;
//
//    /**
//     * Constructor for PingCommand class
//     *
//     * @param sender Player
//     */
//    public PingCommand(Player sender) {
//        this.sender = sender;
//    }
//
//    /**
//     * Getter for sender
//     *
//     * @return Player who sent the command
//     */
//    public Player getSender() {
//        return sender;
//    }
//
//    /**
//     * Executes the command
//     * Prints playerName who is pinging the server
//     */
//    @Override
//    public void execute() {
//        System.out.println("Local: " + sender.getName() + " is pinging the server");
//    }
//}
