package ch.unibas.dmi.dbis.cs108.server.core.model;

import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for serializing game state for network transmission
 */
public class GameStateSerializer {
    /** Logger to log logging */
    private static final Logger LOGGER = Logger.getLogger(GameStateSerializer.class.getName());
    /** The gameState object related to this */
    private final GameState gameState;

    /**
     * Creates a new GameStateSerializer Object.
     *
     * @param gameState the gameState object related to this GameStateSerializer
     */
    public GameStateSerializer(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Create a detailed status message with complete state information
     */
    public String createDetailedStatusMessage() {
        ReadWriteLock lock = gameState.getStateLock();
        lock.readLock().lock();
        try {
            StringBuilder status = new StringBuilder("SYNC$");
            status.append("gameRound=").append(gameState.getTurnManager().getGameRound()).append(",");
            status.append("playerRound=").append(gameState.getTurnManager().getPlayerTurn()).append(",");

            // Add player details
            List<Player> players = gameState.getPlayers();
            status.append("players=");
            for (Player player : players) {
                status.append(player.getName()).append("[");
                status.append("runes=").append(player.getRunes()).append(",");
                status.append("energy=").append(player.getEnergy()).append(",");
                status.append("tiles=").append(player.getOwnedTiles().size());
                status.append("]:");

                // Add artifacts
                status.append("artifacts[");
                for (Artifact artifact : player.getArtifacts()) {
                    status.append(artifact.getId()).append(",");
                }
                status.append("];");
            }

            return status.toString();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating status message", e);
            return "ERROR$Failed to create status";
        } finally {
            lock.readLock().unlock();
        }
    }
}