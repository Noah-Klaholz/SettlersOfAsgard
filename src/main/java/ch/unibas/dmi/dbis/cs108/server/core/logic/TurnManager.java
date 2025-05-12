package ch.unibas.dmi.dbis.cs108.server.core.logic;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors.StructureBehaviorRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Monument;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

/**
 * This class manages the turn and player related logic and updates the gameState.
 */
public class TurnManager {
    /**
     * The gameState object managed by this turnManager
     */
    private final GameState gameState;
    /**
     * Registry for handling structure effects
     */
    private final StructureBehaviorRegistry structureBehaviorRegistry;

    /**
     * Initializes the turnManager. Sets the player- and gameRound to 0.
     *
     * @param gameState the gameState to manage.
     */
    public TurnManager(GameState gameState) {
        this.gameState = gameState;
        gameState.setGameRound(0);
        gameState.setPlayerRound(0);
        gameState.setPlayerTurn(null);
        structureBehaviorRegistry = new StructureBehaviorRegistry();
    }

    /**
     * Advances to the next turn. Handles:
     * - Correct Assignment of the next player whose turn it is.
     * - Updating the gameState (method call).
     * - Distributing resources for the next player.
     */
    public void nextTurn() {
        gameState.getStateLock().writeLock().lock();
        try {
            if (gameState.getPlayerTurn() == null) {
                initializeFirstTurn();
                return;
            }

            Player oldPlayer = gameState.findPlayerByName(gameState.getPlayerTurn());
            endTurn(oldPlayer);

            int nextPosition = (gameState.getPlayerRound() + 1) % gameState.getPlayers().size();
            gameState.setPlayerTurn(gameState.getPlayers().get(nextPosition).getName());
            gameState.setPlayerRound(nextPosition);

            if (nextPosition == 0) {
                gameState.setGameRound(gameState.getGameRound() + 1);
            }

            distributeResources(gameState.findPlayerByName(gameState.getPlayerTurn()));


        } finally {
            gameState.getStateLock().writeLock().unlock();
        }
    }

    /**
     * Ends the turn for the given player by deactivating all their activated purchasable entities.
     *
     * @param oldPlayer The player whose turn is ending.
     */
    private void endTurn(Player oldPlayer) {
        if (oldPlayer == null) return;
        if (oldPlayer.getPurchasableEntities() != null) {
            oldPlayer.getPurchasableEntities().forEach(purchasableEntity -> {
                if (purchasableEntity.isActivated()) {
                    purchasableEntity.setActivated(false);
                }
                if (purchasableEntity.isDisabled()) {
                    purchasableEntity.disabledTurn();
                }
            });
        }
        if (oldPlayer.getMonuments() != null) {
            oldPlayer.getMonuments().forEach(monument -> {
                if (monument.isDisabled()) {
                    monument.disabledTurn();
                }
            });
        }
        oldPlayer.setRoundBoughtTiles(0);
    }

    /**
     * Initializes the first turn. Sets the playerTurn and gives him the resources.
     * Updates the metadata.
     */
    private void initializeFirstTurn() {
        Player firstPlayer = gameState.getPlayers().get(0);
        gameState.setPlayerTurn(firstPlayer.getName());
        distributeResources(firstPlayer);
    }

    /**
     * Reset the metadata.
     */
    public void reset() {
        gameState.setGameRound(0);
        gameState.setPlayerRound(0);
        gameState.setPlayerTurn(null);
    }

    /**
     * Distributes resources to the player based on their owned tiles and structures.
     *
     * @param player The player to distribute resources to.
     */
    private void distributeResources(Player player) {
        // Tile income
        player.getOwnedTiles().forEach(tile -> {
            // Handle Tile - Rune Generation based on the Resource Value of the tile
            int val = tile.getResourceValue();
            calcAndAddRunes(player, tile, val);
            // Handle Structure - Rune Generation based on the ressource value of the entity
            if (tile.hasEntity()) {
                GameEntity ent = tile.getEntity();
                // Handle for Structures
                if (ent instanceof Structure) {
                    Structure entity = (Structure) tile.getEntity();
                    int value = entity.getResourceValue();
                    if (entity.isStructure() && !entity.isDisabled()) {
                        calcAndAddRunes(player, tile, value);
                        // All structures except rune table and the active trap have a passive effect which should be used -> the latter only gets used when buying the tile
                        if (!entity.getName().equals("Rune Table") && !entity.getName().equals("ActiveTrap")) {
                            structureBehaviorRegistry.execute(entity, gameState, player); // Do passive effects -> each structure except Rune Table has one
                        }
                    }
                    // Handle for Monuments
                } else if (ent instanceof Monument mon) {
                    if (!mon.isDisabled()) {
                        int value = mon.getRunes();
                        if (mon.isSet() && player.hasCompleteSet(mon)) {
                            value *= SETTINGS.Config.SET_BONUS_MULTIPLIER.getValue();
                        }
                        calcAndAddRunes(player, tile, value);
                    }
                }
            }
        });
    }

    /**
     * Helper method for calculating and adding the adjusted value of generated runes
     *
     * @param player the player
     * @param tile   the tile
     * @param value  the static value of runes
     */
    private void calcAndAddRunes(Player player, Tile tile, int value) {
        if (tile.hasRiver()) {
            value = (int) (value * player.getStatus().get(Status.BuffType.RIVER_RUNE_GENERATION) * tile.getStatus().get(Status.BuffType.RIVER_RUNE_GENERATION));
        }
        value = (int) (value * player.getStatus().get(Status.BuffType.RUNE_GENERATION) * tile.getStatus().get(Status.BuffType.RUNE_GENERATION));
        player.addRunes(value);
    }

    /**
     * Checks if the gameRound is complete.
     *
     * @return true if the gameRound is complete, false otherwise.
     */
    public boolean isGameRoundComplete() {
        return gameState.getPlayerRound() == gameState.getPlayers().size() - 1;
    }

    /**
     * Gives each player their final scores.
     * This method makes use of the already existing distributeResources method and simulates a new turn for each of the players.
     */
    public void giveFinalScores() {
        for (Player player : gameState.getPlayers()) {
            distributeResources(player);
        }
    }
}