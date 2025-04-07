package ch.unibas.dmi.dbis.cs108.client.core.commands;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Tile;
import ch.unibas.dmi.dbis.cs108.client.core.events.EventDispatcher;
import ch.unibas.dmi.dbis.cs108.client.core.events.TilePurchasedEvent;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.core.commands.*;

/**
 * Command to buy a tile in the game.
 * This command is executed when a player wants to purchase a tile.
 */
public class BuyTileCommand implements Command {
    private final Player player;
    private final Tile tile;

    /**
     * Constructor for BuyTileCommand.
     *
     * @param player The player who wants to buy the tile.
     * @param tile   The tile to be purchased.
     */
    public BuyTileCommand(Player player, Tile tile) {
        this.player = player;
        this.tile = tile;
    }

    /**
     * Execute the command to buy a tile.
     *
     * @param gameState The current game state.
     * @param dispatcher The event dispatcher to handle events.
     */
    @Override
    public void execute(GameState gameState, EventDispatcher dispatcher) {
        gameState.buyTile(player, tile);
        dispatcher.dispatch(new TilePurchasedEvent(player, tile));
    }
}
