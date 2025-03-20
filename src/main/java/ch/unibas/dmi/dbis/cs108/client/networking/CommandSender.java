package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.client.core.commands.ChatCommand;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.MessageFormatter;


public class CommandSender {
    private final SocketHandler socketHandler;
    private final MessageFormatter formatter;


    public CommandSender(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
        this.formatter = new MessageFormatter();
    }

    public void sendChatCommand(ChatCommand chatCommand) {
        String message = MessageFormatter.formatChatMessage(
                chatCommand.getSender().getId(),
                chatCommand.getMessage()
        );
        socketHandler.send(message);
    }

    public void sendChangeName(Player player, String newName) {
        String message = formatter.formatNameChange(player.getId(), newName);
        socketHandler.send(message);
    }

    public void sendDisconnect(Player player) {
        String message = formatter.formatDisconnect(player.getId());
        socketHandler.send(message);
    }

    public void sendPing(Player player) {
        String message = formatter.formatPing(player.getId());
        socketHandler.send(message);
    }

    public void sendRegister(Player player) {
        String message = formatter.formatRegister(player.getId(), player.getName());
        socketHandler.send(message);
    }


}
