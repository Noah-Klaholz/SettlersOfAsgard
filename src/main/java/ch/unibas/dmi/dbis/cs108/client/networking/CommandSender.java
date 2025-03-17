package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.client.commands.CommandFactory;
import ch.unibas.dmi.dbis.cs108.client.commands.GameCommand;

public class CommandSender {
    private GameClient client;

    public CommandSender(GameClient client) {
        this.client = client;
    }

    public void sendCommand(String type, String data) {
        GameCommand command = CommandFactory.createCommand(type, data);
        client.sendMessage(command.execute());
    }
}
