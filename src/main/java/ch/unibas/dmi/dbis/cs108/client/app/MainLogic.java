package ch.unibas.dmi.dbis.cs108.client.app;

import ch.unibas.dmi.dbis.cs108.client.core.commands.BuyTileCommand;
import ch.unibas.dmi.dbis.cs108.shared.entities.*;
import ch.unibas.dmi.dbis.cs108.client.core.events.*;
import ch.unibas.dmi.dbis.cs108.client.core.game.GameEngine;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.client.core.commands.*;

import java.util.ArrayList;
import java.util.List;

public class MainLogic {
    public static void main(String[] args) {
        // Initialize game state and board.
        GameState gameState = new GameState();
        Board board = gameState.getBoard();
        board.initBoard(7,8);  // Create 5 fields.

        // Create players.
        Player alice = new Player("Alice");
        Player bob = new Player("Bob");
        gameState.addPlayer(alice);
        gameState.addPlayer(bob);

        // Create game engine.
        GameEngine engine = new GameEngine(gameState);

        // Register a simple event listener for logging.
        engine.getEventDispatcher().register(event -> {
            if (event instanceof TilePurchasedEvent) {
                TilePurchasedEvent fpe = (TilePurchasedEvent) event;
                System.out.println("Event: " + fpe.getPlayer().getName() +
                        " purchased tile " + fpe.getTile().getTileID());
            }
        });

        // --- Simulate a Single Turn ---
        List<Command> turnCommands = new ArrayList<>();
        // Alice buys field 0.
        Tile tile0 = board.getTile(0);
        turnCommands.add(new BuyTileCommand(alice, tile0));
        engine.processTurn(turnCommands);

        // --- Simulate a Full Game Round ---
        GameRound round = gameState.getCurrentGameRound();

        // Create player rounds.
        PlayerRound aliceRound = new PlayerRound();
        aliceRound.setActivePlayer(alice);
        // For demo: if field 1 is available, Alice buys it.
        Tile tile1 = board.getTile(1);
        if (!tile1.isPurchased()) {
            aliceRound.addCommand(new BuyTileCommand(alice, tile1));
        }

        PlayerRound bobRound = new PlayerRound();
        bobRound.setActivePlayer(bob);
        // Bob buys field 2 if available.
        Tile tile2 = board.getTile(2);
        if (!tile2.isPurchased()) {
            bobRound.addCommand(new BuyTileCommand(bob, tile2));
        }

        round.addPlayerRound(aliceRound);
        round.addPlayerRound(bobRound);

        engine.processGameRound(round);

        // --- Final State Summary ---
        System.out.println("Final Game State:");
        for (Player player : gameState.getPlayers()) {
            System.out.println(player);
        }
    }
}