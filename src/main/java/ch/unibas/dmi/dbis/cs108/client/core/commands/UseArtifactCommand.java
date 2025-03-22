package ch.unibas.dmi.dbis.cs108.client.core.commands;

/**
 * The UseArtifactCommand is a command that is used to use an artifact
 */
public class UseArtifactCommand implements Command {

    private final String data;

    /**
     * Constructor for the UseArtifactCommand
     *
     * @param data the data for the command
     */
    public UseArtifactCommand(String data) {
        this.data = data;
    }

    /**
     * Executes the command
     *
     * @return the command as a String
     */
    @Override
    public void execute() {
        System.out.println("USE_ARTIFACT:" + data);
    }
}
