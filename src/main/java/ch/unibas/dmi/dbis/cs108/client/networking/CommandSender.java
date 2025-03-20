package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.client.core.commands.ChatCommand;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.MessageFormatter;

/**
 * CommandSender class is responsible for sending commands to the server
 */
public class CommandSender {
    private final SocketHandler socketHandler;
    private final MessageFormatter formatter;

    /**
     * Constructor
     * @param socketHandler SocketHandler
     */
    public CommandSender(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
        this.formatter = new MessageFormatter();
    }

    /**
     * Sends a chat command to the server
     * @param chatCommand ChatCommand
     */
    public void sendChatCommand(ChatCommand chatCommand) {
        String message = MessageFormatter.formatChatMessage(
                chatCommand.getSender().getId(),
                chatCommand.getMessage()
        );
        socketHandler.send(message);
    }

    /**
     * Sends a change name command to the server
     * @param player Player
     * @param newName String
     */
    public void sendChangeName(Player player, String newName) {
        String message = formatter.formatNameChange(player.getId(), newName);
        socketHandler.send(message);
    }

    /**
     * Sends a disconnect command to the server
     * @param player Player
     */
    public void sendDisconnect(Player player) {
        String message = formatter.formatDisconnect(player.getId());
        socketHandler.send(message);
    }

    /**
     * Sends a ping command to the server
     * @param player Player
     */
    public void sendPing(Player player) {
        String message = formatter.formatPing(player.getId());
        socketHandler.send(message);
    }

    /**
     * Sends a register command to the server
     * @param player Player
     */
    public void sendRegister(Player player) {
        String message = formatter.formatRegister(player.getId(), player.getName());
        socketHandler.send(message);
    }


}
