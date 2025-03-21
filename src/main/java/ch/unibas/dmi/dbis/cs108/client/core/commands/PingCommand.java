package ch.unibas.dmi.dbis.cs108.client.core.commands;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;

public class PingCommand implements Command {
    private final Player sender;

    public PingCommand(Player sender) {
        this.sender = sender;
    }

    public Player getSender() {
        return sender;
    }

    @Override
    public void execute() {
        System.out.println("Local: " + sender.getName() + " is pinging the server");
    }
}
