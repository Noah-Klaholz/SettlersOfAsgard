package ch.unibas.dmi.dbis.cs108.server.core.actions;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

import java.util.concurrent.locks.ReadWriteLock;

public class TileActionHandler {
    private final GameState gameState;
    private final ReadWriteLock gameLock;

    public TileActionHandler(GameState gameState, ReadWriteLock gameLock) {
        this.gameState = gameState;
        this.gameLock = gameLock;
    }

    public boolean buyTile(int x, int y, String playerName) {
        gameLock.writeLock().lock();
        try {
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
            gameLock.writeLock().unlock();
        }
    }

    private Player findPlayerByName(String playerName) {
        for (Player p : gameState.getPlayers()) {
            if (p.getName().equals(playerName)) {
                return p;
            }
        }
        return null;
    }
}