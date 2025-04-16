package ch.unibas.dmi.dbis.cs108.server.core.actions;

import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors.StatueBehaviorRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.StatueParameters;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.server.core.model.BoardManager;

import java.util.concurrent.locks.ReadWriteLock;

public class StatueActionHandler {
    private final GameState gameState;
    private final ReadWriteLock gameLock;

    public StatueActionHandler(GameState gameState, ReadWriteLock gameLock) {
        this.gameState = gameState;
        this.gameLock = gameLock;
    }


    public boolean buyStatue(int statueId, String playerName) {
        // Will be implemented in the future when decided how to do it
        return true;
    }

    public boolean upgradeStatue(int x, int y, int statueId, String playerName) {
        gameLock.writeLock().lock();
        try {
            // Get the player
            Player player = gameState.findPlayerByName(playerName);
            if (player == null) {
                return false;
            }

            // Find the statue on the board at the given coordinates
            BoardManager boardManager = gameState.getBoardManager();
            Tile tile = boardManager.getTile(x, y);
            if (tile == null || !tile.hasEntity() || !tile.getOwner().equals(playerName)) {
                return false;
            }

            // Check if the statue on the tile matches the requested statue ID
            PurchasableEntity entity = tile.getEntity();
            Statue statue;
            if (!(entity instanceof Statue) || entity.getId() != statueId) {
                return false;
            } else {
                statue = (Statue) entity;
            }

            // Check if statue can be upgraded (max level is 3)
            if (statue.getLevel() >= 3) {
                return false;
            }

            // Calculate upgrade cost
            int upgradeCost = statue.getUpgradePrice();

            // Check if player can afford the upgrade
            if (player.getRunes() < upgradeCost) {
                return false;
            }

            // Deduct runes and upgrade the statue
            player.addRunes(-upgradeCost);
            statue.upgrade();

            return true;
        } finally {
            gameLock.writeLock().unlock();
        }
    }

    public boolean useStatue(int x, int y, int statueId, String playerName, String params) {
        gameLock.writeLock().lock();
        try {
            // Get the player
            Player player = gameState.findPlayerByName(playerName);
            if (player == null) {
                return false;
            }

            // Find the statue on the board at the given coordinates
            BoardManager boardManager = gameState.getBoardManager();
            Tile tile = boardManager.getTile(x, y);
            if (tile == null || !tile.hasEntity() || !tile.getOwner().equals(playerName)) {
                return false;
            }

            // Check if the statue on the tile matches the requested statue ID
            PurchasableEntity entity = tile.getEntity();
            Statue statue;
            if (!(entity instanceof Statue) || entity.getId() != statueId) {
                return false;
            } else {
                statue = (Statue) entity;
            }

            // Parse parameters and create StatueParameters object
            StatueParameters statueParams = parseParameters(params);

            // Execute the statue effect using the StatueBehaviorRegistry
            StatueBehaviorRegistry registry = gameState.getStatueBehaviorRegistry();
            return registry.executeStatue(statue, gameState, player, statueParams);
        } finally {
            gameLock.writeLock().unlock();
        }
    }

    /**
     * Parses a parameter string in format "KEY1:VALUE1;KEY2:VALUE2;..."
     * into a StatueParameters object.
     *
     * @param paramsString The parameter string
     * @return StatueParameters object with parsed values
     */
    private StatueParameters parseParameters(String paramsString) {
        StatueParameters.Builder builder = new StatueParameters.Builder();

        if (paramsString == null || paramsString.isEmpty()) {
            return builder.build();
        }

        String[] paramPairs = paramsString.split(";");
        for (String pair : paramPairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) {
                continue;
            }

            String key = keyValue[0].toUpperCase();
            String value = keyValue[1];

            switch (key) {
                case "PLAYER":
                    Player targetPlayer = gameState.getPlayerByName(value);
                    if (targetPlayer != null) {
                        builder.withPlayer(targetPlayer);
                    }
                    break;
                case "TILE":
                    String[] coordinates = value.split(",");
                    if (coordinates.length == 2) {
                        try {
                            int tileX = Integer.parseInt(coordinates[0]);
                            int tileY = Integer.parseInt(coordinates[1]);
                            builder.withTile(tileX, tileY);
                        } catch (NumberFormatException e) {
                            // Invalid coordinates format
                        }
                    }
                    break;
                case "STRUCTURE":
                    try {
                        int structureId = Integer.parseInt(value);
                        Structure structure = findStructureById(structureId);
                        if (structure != null) {
                            builder.withStructure(structure);
                        }
                    } catch (NumberFormatException e) {
                        // Invalid structure ID format
                    }
                    break;
                case "ARTIFACT":
                    try {
                        int artifactId = Integer.parseInt(value);
                        Artifact artifact = findArtifactById(artifactId);
                        if (artifact != null) {
                            builder.withArtifact(artifact);
                        }
                    } catch (NumberFormatException e) {
                        // Invalid artifact ID format
                    }
                    break;
            }
        }

        return builder.build();
    }

    /**
     * Calculates the cost to upgrade a statue.
     *
     * @param statue The statue to upgrade
     * @return The upgrade cost in runes
     */
    private int calculateUpgradeCost(Statue statue) {
        // This logic might need adjustment based on your game's rules
        return statue.getPrice() * statue.getLevel();
    }

    /**
     * Finds a structure by its ID.
     *
     * @param structureId The structure ID
     * @return The structure or null if not found
     */
    private Structure findStructureById(int structureId) {
        // Implementation depends on how structures are stored in your game state
        return EntityRegistry.getStructure(structureId);
    }

    /**
     * Finds an artifact by its ID.
     *
     * @param artifactId The artifact ID
     * @return The artifact or null if not found
     */
    private Artifact findArtifactById(int artifactId) {
        // Implementation depends on how artifacts are stored in your game state
        return EntityRegistry.getArtifact(artifactId);
    }
}