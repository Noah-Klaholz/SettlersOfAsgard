package ch.unibas.dmi.dbis.cs108.client.networking;

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

    /**
     * Sends a command to the server
     * @param type Type of command
     * @param data Data of command
     */
    public void sendCommand(String type, String data) {
        GameCommand command = CommandFactory.createCommand(type, data);
        client.sendMessage(command.execute());
    }

    public void sendRegister(Player player) {

    }

    public void sendDisconnect(Player player){

    }
}
