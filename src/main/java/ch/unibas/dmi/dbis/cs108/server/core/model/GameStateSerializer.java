package ch.unibas.dmi.dbis.cs108.server.core.model;

import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Monument;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Logger;

/**
 * Responsible for serializing game state for network transmission
 */
public class GameStateSerializer {
    /**
     * Logger to log logging
     */
    private static final Logger LOGGER = Logger.getLogger(GameStateSerializer.class.getName());
    /**
     * The gameState object related to this
     */
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
     *
     * @return the detailed status message
     */
    public String createDetailedStatusMessage() {
        ReadWriteLock lock = gameState.getStateLock();
        lock.readLock().lock();
        try {
            StringBuilder sb = new StringBuilder("SYNC$"); // CommandType = SYNCHRONIZE

            // 1. Game Meta
            sb.append("META:")
                    .append(gameState.getGameRound()).append(",")
                    .append(gameState.getPlayerRound()).append(",")
                    .append(gameState.getPlayerTurn()).append("|");

            // 2. All Players
            sb.append("PLAYERS:");
            for (Player p : gameState.getPlayers()) {
                sb.append(p.getName()).append("{")
                        .append("R:").append(p.getRunes()).append(",")
                        .append("E:").append(p.getEnergy()).append(",")

                        // Owned tiles (IDs)
                        .append("T:[");
                for (Tile t : p.getOwnedTiles()) {
                    sb.append(t.getTileID()).append(",");
                }
                if (!p.getOwnedTiles().isEmpty()) sb.deleteCharAt(sb.length() - 1);
                sb.append("],");

                // Artifacts
                sb.append("A:[");
                for (Artifact a : p.getArtifacts()) {
                    sb.append(a.getId()).append(",");
                }
                if (!p.getArtifacts().isEmpty()) sb.deleteCharAt(sb.length() - 1);
                sb.append("],");

                // Purchasable entities
                sb.append("PE:[");
                for (PurchasableEntity pe : p.getPurchasableEntities()) {
                    sb.append(pe.getId()).append(",");
                }
                if (!p.getPurchasableEntities().isEmpty()) sb.deleteCharAt(sb.length() - 1);
                sb.append("],");

                // Status
                sb.append("ST:{")
                        .append("RG:").append(p.getStatus().get(Status.BuffType.RUNE_GENERATION)).append(",")
                        .append("EG:").append(p.getStatus().get(Status.BuffType.ENERGY_GENERATION)).append(",")
                        .append("RR:").append(p.getStatus().get(Status.BuffType.RIVER_RUNE_GENERATION)).append(",")
                        .append("SP:").append(p.getStatus().get(Status.BuffType.SHOP_PRICE)).append(",")
                        .append("AC:").append(p.getStatus().get(Status.BuffType.ARTIFACT_CHANCE)).append(",")
                        .append("DB:").append(p.getStatus().get(Status.BuffType.DEBUFFABLE))
                        .append("}};");
            }
            sb.append("|");

            // 3. All Tiles (Simplified and consistent board section)
            sb.append("BOARD:");
            Tile[][] tiles = gameState.getBoardManager().getBoard().getTiles();
            for (int x = 0; x < tiles.length; x++) {
                for (int y = 0; y < tiles[x].length; y++) {
                    Tile t = tiles[x][y];
                    GameEntity e = t.getEntity();

                    sb.append(x).append(",").append(y).append("{");
                    sb.append("HE=").append(t.hasEntity() ? 1 : 0).append("|");
                    sb.append("O=").append(t.getOwner() != null ? t.getOwner() : "null").append("|");
                    sb.append("P=").append(t.getPrice()).append("|");

                    // Entity section
                    if (e == null) {
                        sb.append("ENT=NONE|");
                    } else if (e instanceof Statue statue) {
                        sb.append("ENT=STA,")
                                .append(statue.getId()).append(",")
                                .append("DI=").append(statue.getDisabled()).append(",")
                                .append("AC=").append(statue.isActivated()).append(",")
                                .append("LV=").append(statue.getLevel()).append("|");
                    } else if (e instanceof Monument monument) {
                        sb.append("ENT=MON,")
                                .append(monument.getId()).append(",")
                                .append("DI=").append(monument.isDisabled()).append("|");
                    } else if (e instanceof Structure structure) {
                        sb.append("ENT=STR,")
                                .append(structure.getId()).append(",")
                                .append("DI=").append(structure.isDisabled()).append(",")
                                .append("AC=").append(structure.isActivated()).append("|");
                    }

                    sb.append("AR=").append(t.getArtifact() != null ? t.getArtifact().getId() : "null").append("|");
                    sb.append("W=").append(t.getWorld()).append("|");
                    sb.append("PU=").append(t.isPurchased() ? 1 : 0).append("|");
                    sb.append("RV=").append(t.getResourceValue()).append("|");
                    sb.append("HR=").append(t.hasRiver() ? 1 : 0).append("|");
                    sb.append("ID=").append(t.getTileID()).append("|");

                    // Tile status
                    sb.append("ST=RG:").append(t.getStatus().get(Status.BuffType.RUNE_GENERATION)).append(",");
                    sb.append("EG:").append(t.getStatus().get(Status.BuffType.ENERGY_GENERATION)).append(",");
                    sb.append("RR:").append(t.getStatus().get(Status.BuffType.RIVER_RUNE_GENERATION)).append(",");
                    sb.append("SP:").append(t.getStatus().get(Status.BuffType.SHOP_PRICE)).append(",");
                    sb.append("AC:").append(t.getStatus().get(Status.BuffType.ARTIFACT_CHANCE)).append(",");
                    sb.append("DB:").append(t.getStatus().get(Status.BuffType.DEBUFFABLE));
                    sb.append("};");
                }
            }
            return sb.toString();
        } finally {
            lock.readLock().unlock();
        }
    }
}
