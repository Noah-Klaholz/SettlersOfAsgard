package ch.unibas.dmi.dbis.cs108.client.commands;

public class UseArtifactCommand implements GameCommand {

    private String data;

    public UseArtifactCommand(String data) {
        this.data = data;
    }

    @Override
    public String execute() {
        return "USE_ARTIFACT:" + data;
    }
}
