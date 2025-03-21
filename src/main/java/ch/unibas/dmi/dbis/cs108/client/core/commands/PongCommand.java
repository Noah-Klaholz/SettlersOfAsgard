package ch.unibas.dmi.dbis.cs108.client.core.commands;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;

public class PongCommand implements Command {
    private final Player sender;
    private final String targetId;

    public PongCommand(Player sender, String targetId) {
        this.sender = sender;
        this.targetId = targetId;
    }

    public Player getSender() {
        return sender;
    }

    public String getTargetId() {
        return targetId;
    }

    @Override
    public void execute() {
        System.out.println("Local: " + sender.getName() + " is ponging the server with message: " + targetId);
    }
}
