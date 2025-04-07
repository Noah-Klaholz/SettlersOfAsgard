package ch.unibas.dmi.dbis.cs108.client.core.commands;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Tile;
import ch.unibas.dmi.dbis.cs108.client.core.events.EventDispatcher;
import ch.unibas.dmi.dbis.cs108.client.core.events.TilePurchasedEvent;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.core.commands.*;

public class BuyTileCommand implements Command {
    private final Player player;
    private final Tile tile;

    public BuyTileCommand(Player player, Tile tile) {
        this.player = player;
        this.tile = tile;
    }

    @Override
    public void execute(GameState gameState, EventDispatcher dispatcher) {
        gameState.buyTile(player, tile);
        dispatcher.dispatch(new TilePurchasedEvent(player, tile));
    }
}
