package ch.unibas.dmi.dbis.cs108.client.commands;


public class BuyTileCommand implements GameCommand {

    private String data;

    /**
     * Constructor for the BuyTileCommand
     * @param data the data for the command
     */
    public BuyTileCommand(String data) {
        this.data = data;
    }

    /**
     * Executes the command
     * @return the command as a String
     */
    @Override
    public String execute() {
        return "BUY_TILE:" + data;
    }
}
