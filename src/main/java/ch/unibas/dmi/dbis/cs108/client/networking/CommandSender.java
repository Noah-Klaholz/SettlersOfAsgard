package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.client.core.commands.ChatCommand;
import ch.unibas.dmi.dbis.cs108.client.core.commands.CommandFactory;
import ch.unibas.dmi.dbis.cs108.client.core.commands.GameCommand;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.networking.protocol.MessageFormatter;


public class CommandSender {
    private final SocketHandler socketHandler;
    private final MessageFormatter formatter;


    public CommandSender(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
        this.formatter = new MessageFormatter();
    }


    public void sendCommand(String type, String data) {

    }

    public void sendRegister(Player player) {

    }

    public void sendDisconnect(Player player){

    }

    public void sendPing(Player player){

    }

    public void sendChatCommand(ChatCommand chatCommand){

    }

    public void sendChangeName(Player player, String newName){

    }


}
