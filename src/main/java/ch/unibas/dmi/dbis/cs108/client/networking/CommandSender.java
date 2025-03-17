package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.client.commands.CommandFactory;
import ch.unibas.dmi.dbis.cs108.client.commands.GameCommand;

/**
 * CommandSender class is responsible for sending commands to the server
 */
public class CommandSender {
    private GameClient client;

    /**
     * Constructor for CommandSender
     * @param client GameClient
     */
    public CommandSender(GameClient client) {
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
}
