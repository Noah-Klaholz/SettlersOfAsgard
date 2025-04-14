package ch.unibas.dmi.dbis.cs108.server.core.model;

import ch.unibas.dmi.dbis.cs108.shared.entities.Player;
import ch.unibas.dmi.dbis.cs108.shared.entities.artifacts.Artifact;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for serializing game state for network transmission
 */
public class GameStateSerializer {
    private static final Logger LOGGER = Logger.getLogger(GameStateSerializer.class.getName());

    private final GameState gameState;

    public GameStateSerializer(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Create a serialized state message for network transmission
     */
    public String createStateMessage() {
        ReadWriteLock lock = gameState.getStateLock();
        lock.readLock().lock();
        try {
            StringBuilder state = new StringBuilder("STATE$");
            state.append("round=").append(gameState.getTurnManager().getGameRound()).append(",");
            state.append("player_round=").append(gameState.getTurnManager().getPlayerRound()).append(",");
            state.append("current_player=").append(gameState.getTurnManager().getPlayerTurn()).append(",");

            // Add player basic info
            List<Player> players = gameState.getPlayerManager().getPlayers();
            for (Player player : players) {
                state.append("player=").append(player.getName()).append(",");
            }

            return state.toString();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating state message", e);
            return "ERROR$Failed to create state";
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Create a detailed status message with complete state information
     */
    public String createDetailedStatusMessage() {
        ReadWriteLock lock = gameState.getStateLock();
        lock.readLock().lock();
        try {
            StringBuilder status = new StringBuilder("STATUS$");
            status.append("round=").append(gameState.getTurnManager().getGameRound()).append(",");
            status.append("turn=").append(gameState.getTurnManager().getPlayerTurn()).append(",");

            // Add player details
            List<Player> players = gameState.getPlayerManager().getPlayers();
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
                    status.append(artifact.getArtifactID()).append(",");
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