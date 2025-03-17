package ch.unibas.dmi.dbis.cs108.client.networking;

public class CommandSender {
    private GameClient client;

    public CommandSender(GameClient client) {
        this.client = client;
    }

    public void sendCommand(String type, String data) {
    }
}
