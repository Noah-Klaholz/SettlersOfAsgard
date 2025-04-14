package ch.unibas.dmi.dbis.cs108.server.core.logic;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.Player;
import ch.unibas.dmi.dbis.cs108.shared.entities.Tile;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Encapsulates game rules and validation logic
 */
public class GameRules {
    private final GameState gameState;
    private final ReadWriteLock stateLock = new ReentrantReadWriteLock();

    public GameRules(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Validates and executes tile purchase according to game rules
     */
    public boolean buyTile(int x, int y, String playerName) {
        try {
            stateLock.writeLock().lock();

            Tile tile = gameState.getBoardManager().getBoard().getTileByCoordinates(x, y);
            if (tile == null || tile.isPurchased()) {
                return false;
            }

            Player player = findPlayerByName(playerName);
            if (player == null || player.getRunes() < tile.getPrice()) {
                return false;
            }

            player.removeRunes(tile.getPrice());
            tile.setPurchased(true);
            player.addOwnedTile(tile);
            return true;
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Advances to the next player's turn following game rules
     */
    public void nextTurn() {
        try {
            stateLock.writeLock().lock();

            if (gameState.getTurnManager().getPlayerTurn() == null) {
                initializeFirstTurn();
                return;
            }

            int nextPosition = (gameState.getTurnManager().getPlayerRound() + 1) % gameState.getPlayerManager().getPlayers().size();
            boolean newRound = nextPosition == 0;

            if (newRound) {
                gameState.getTurnManager().setGameRound(gameState.getTurnManager().getGameRound() + 1);
            }

            gameState.getTurnManager().setPlayerRound(nextPosition);
            Player nextPlayer = gameState.getPlayerManager().getPlayers().get(nextPosition);
            gameState.getTurnManager().setPlayerTurn(nextPlayer.getName());
            resourcesIncome(nextPlayer);
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    // Other game rule methods with proper synchronization

    private Player findPlayerByName(String name) {
        return gameState.getPlayerManager().getPlayers().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private void initializeFirstTurn() {
        // Implementation
    }

    private void resourcesIncome(Player player) {
        // Implementation
    }

    // Additional game rule methods...
}