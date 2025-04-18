package ch.unibas.dmi.dbis.cs108.server.core.actions;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import ch.unibas.dmi.dbis.cs108.shared.utils.RandomGenerator;

import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;

public class TileActionHandler {
    private final GameState gameState;
    private final ReadWriteLock gameLock;

    public TileActionHandler(GameState gameState, ReadWriteLock gameLock) {
        this.gameState = gameState;
        this.gameLock = gameLock;
    }

    /**
     * Buys a tile for the player
     *
     * @param x          x coordinate of the tile
     * @param y          y coordinate of the tile
     * @param playerName name of the player
     * @return true if the tile was bought successfully, false otherwise
     */
    public boolean buyTile(int x, int y, String playerName) {
        gameLock.writeLock().lock();
        try {
            Tile tile = gameState.getBoardManager().getBoard().getTileByCoordinates(x, y);
            if (tile == null || tile.isPurchased()) {
                return false;
            }

            Player player = findPlayerByName(playerName);
            if (player == null || player.getRoundBoughtTiles() < SETTINGS.Config.PURCHASABLE_TILES_PER_ROUND.getValue() && !player.buy(tile.getPrice())) {
                return false;
            }

            tile.setPurchased(true);
            player.addOwnedTile(tile);
            player.addBoughtTile();

            // Players always have a chance to randomly find an artifact when they buy a tile based on their artifact chance
            if (tile.getArtifact() == null && RandomGenerator.chance((int) player.getStatus().get(Status.BuffType.ARTIFACT_CHANCE))) {
                Artifact artifact = EntityRegistry.getRandomArtifact();
                tile.setArtifact(artifact);
            }

            // Check if the Tile holds an artifact, and if so add it to the player
            if (tile.getArtifact() != null) {
                player.getArtifacts().add(tile.getArtifact());
                tile.setArtifact(null);
            }

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