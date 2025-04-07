package ch.unibas.dmi.dbis.cs108.client.core.commands;

import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Tile;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.core.events.EventDispatcher;

/**
 * Command interface for executing game commands.
 * This interface defines the structure for commands that can be executed in the game.
 */
public interface Command {
    /**
     * Execute the command with the given game state and event dispatcher.
     *
     * @param gameState  The current game state.
     * @param dispatcher The event dispatcher to handle events.
     */
    void execute(GameState gameState, EventDispatcher dispatcher);
}
