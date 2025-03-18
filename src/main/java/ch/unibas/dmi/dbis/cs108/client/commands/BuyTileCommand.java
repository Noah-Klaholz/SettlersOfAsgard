package ch.unibas.dmi.dbis.cs108.client.commands;


public class BuyTileCommand implements GameCommand {

    private String data;

    public BuyTileCommand(String data) {
        this.data = data;
    }

    @Override
    public String execute() {
        return "BUY_TILE:" + data;
    }
}
