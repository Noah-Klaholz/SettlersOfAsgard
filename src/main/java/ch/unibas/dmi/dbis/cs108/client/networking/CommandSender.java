package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.client.core.commands.ChatCommand;
import ch.unibas.dmi.dbis.cs108.client.core.commands.CommandFactory;
import ch.unibas.dmi.dbis.cs108.client.core.commands.GameCommand;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;

/**
 * CommandSender class is responsible for sending commands to the server
 */
public class CommandSender {
    private GameClient client;


    public CommandSender(SocketHandler socketHandler) {
        this.client = client;
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


}
