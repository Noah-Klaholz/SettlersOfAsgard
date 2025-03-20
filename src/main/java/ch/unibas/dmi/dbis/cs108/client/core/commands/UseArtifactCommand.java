package ch.unibas.dmi.dbis.cs108.client.core.commands;

public class UseArtifactCommand implements GameCommand {

    private String data;

    /**
     * Constructor for the UseArtifactCommand
     * @param data the data for the command
     */
    public UseArtifactCommand(String data) {
        this.data = data;
    }

    /**
     * Executes the command
     * @return the command as a String
     */
    @Override
    public String execute() {
        return "USE_ARTIFACT:" + data;
    }
}
