package ch.unibas.dmi.dbis.cs108.server.core.logic;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors.StructureBehaviorRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Monument;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;
import java.util.*;

/**
 * This class manages the turn and player related logic and updates the gameState.
 */
public class TurnManager {
    /** The gameState object managed by this turnManager */
    private final GameState gameState;
    /** The name of the player whose turn it is */
    private String playerTurn;
    /** An integer representing whose turn it is (0 to maxPlayers - 1) */
    private int playerRound;
    /** An integer representing which gameRound it is (0 to 4) */
    private int gameRound;
    /** Registry for handling structure effects */
    private StructureBehaviorRegistry structureBehaviorRegistry;

    /**
     * Initializes the turnManager. Sets the player- and gameRound to 0.
     *
     * @param gameState the gameState to manage.
     */
    public TurnManager(GameState gameState) {
        this.gameState = gameState;
        this.gameRound = 0;
        this.playerRound = 0;
        this.playerTurn = null;
    }

    /**
     * Gets the playerTurn.
     *
     * @return the playerTurn.
     */
    public String getPlayerTurn() {
        return playerTurn;
    }

    /**
     * Sets the playerTurn.
     *
     * @param playerTurn the playerTurn to set.
     */
    public void setPlayerTurn(String playerTurn) {
        this.playerTurn = playerTurn;
    }

    /**
     * Gets the playerRound.
     *
     * @return the playerRound.
     */
    public int getPlayerRound() {
        return playerRound;
    }

    /**
     * Sets the playerRound.
     *
     * @param playerRound the playerRound to set.
     */
    public void setPlayerRound(int playerRound) {
        this.playerRound = playerRound;
    }

    /**
     * Gets the gameRound.
     *
     * @return the gameRound.
     */
    public int getGameRound() {
        return gameRound;
    }

    /**
     * Sets the gameRound.
     *
     * @param gameRound the gameRound to set.
     */
    public void setGameRound(int gameRound) {
        this.gameRound = gameRound;
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
            if (getPlayerTurn() == null) {
                initializeFirstTurn();
                return;
            }

            Player oldPlayer = gameState.findPlayerByName(playerTurn);
            endTurn(oldPlayer);

            int nextPosition = (playerRound + 1) % gameState.getPlayers().size();
            playerTurn = gameState.getPlayers().get(nextPosition).getName();
            playerRound = nextPosition;

            if (nextPosition == 0) {
                gameRound ++;
            }

            updateGameStateMeta();
            distributeResources(gameState.findPlayerByName(playerTurn));


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
        oldPlayer.getPurchasableEntities().forEach(purchasableEntity -> {
            if (purchasableEntity.isActivated()) {
                purchasableEntity.setActivated(false);
            }
            if (purchasableEntity.isDisabled()) {
                purchasableEntity.disabledTurn();
            }
        });
        oldPlayer.setRoundBoughtTiles(0);
    }

    /**
     * Initializes the first turn. Sets the playerTurn and gives him the resources.
     * Updates the metadata.
     */
    private void initializeFirstTurn() {
        Player firstPlayer = gameState.getPlayers().get(0);
        setPlayerTurn(firstPlayer.getName());
        distributeResources(firstPlayer);
        updateGameStateMeta();

    }

    /**
     * Reset the metadata.
     */
    public void reset() {
        gameRound = 0;
        playerRound = 0;
        playerTurn = null;
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
                    if (entity.isStructure()) {
                        calcAndAddRunes(player, tile, value);
                        // All structures except rune table have a passive effect which should be used
                        if (!entity.getName().equals("Rune Table")) {
                            structureBehaviorRegistry.execute(entity,gameState,player); // Do passive effects -> each structure except Rune Table has one
                        }
                    }
                // Handle for Monuments
                } else if (ent instanceof Monument mon) {
                    int value = mon.getRunes();
                    if (mon.isSet() && player.hasCompleteSet(mon)) {
                        value *= SETTINGS.Config.SET_BONUS_MULTIPLIER.getValue();
                    }
                    calcAndAddRunes(player, tile, value);
                }
            }
        });
    }

    /**
     * Helper method for calculating and adding the adjusted value of generated runes
     *
     * @param player the player
     * @param tile the tile
     * @param value the static value of runes
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
        return playerRound == gameState.getPlayers().size() - 1;
    }

    /**
     * Updates the fields in the managed gameState.
     */
    public void updateGameStateMeta() {
        gameState.setGameRound(gameRound);
        gameState.setPlayerTurn(playerTurn);
        gameState.setPlayerRound(playerRound);
    }
}