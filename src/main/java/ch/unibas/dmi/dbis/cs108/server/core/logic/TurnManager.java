// TurnManager.java
package ch.unibas.dmi.dbis.cs108.server.core.logic;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI;
import java.util.*;

public class TurnManager {
    private final GameState gameState;
    private CommunicationAPI communicationApi;
    private String playerTurn;
    private int playerRound;
    private int gameRound;

    public TurnManager(GameState gameState) {
        this.gameState = gameState;
        this.communicationApi = null;
        this.gameRound = 0;
        this.playerRound = 0;
        this.playerTurn = null;
    }

    public void setCommunicationApi(CommunicationAPI communicationApi) {
        this.communicationApi = communicationApi;
    }

    // Add getter and setter methods
    public String getPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(String playerTurn) {
        this.playerTurn = playerTurn;
    }

    public int getPlayerRound() {
        return playerRound;
    }

    public void setPlayerRound(int playerRound) {
        this.playerRound = playerRound;
    }

    public int getGameRound() {
        return gameRound;
    }

    public void setGameRound(int gameRound) {
        this.gameRound = gameRound;
    }

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
        });
    }

    private void initializeFirstTurn() {
        // Start with a random player
        setPlayerRound(0);
        Player firstPlayer = gameState.getPlayers().get(0);
        setPlayerTurn(firstPlayer.getName());

        // Initial resources for first player
        distributeResources(firstPlayer);

    }

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
            int runes = (int) (tile.getResourceValue() * player.getStatus().get(Status.BuffType.RUNE_GENERATION) * tile.getStatus().get(Status.BuffType.RUNE_GENERATION));
            if (tile.hasRiver()) {
                runes = (int) (runes * player.getStatus().get(Status.BuffType.RIVER_RUNE_GENERATION));
            }
            player.addRunes(runes);
            if (tile.hasEntity()) {
                PurchasableEntity entity = tile.getEntity();
                int value = entity.getResourceValue(); // Either energy or runes
                if (entity.isStatue()) {
                    if (tile.hasRiver()) {
                        value = (int) (value * player.getStatus().get(Status.BuffType.RIVER_RUNE_GENERATION));
                    }
                    value = (int) (value * player.getStatus().get(Status.BuffType.RUNE_GENERATION));
                    player.addRunes(value);
                } else {
                    value = (int) (value * player.getStatus().get(Status.BuffType.ENERGY_GENERATION));
                    player.addEnergy(value);
                }
            }
        });
    }

    private int indexOfCurrentPlayer() {
        List<Player> players = gameState.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(playerTurn)) {
                return i;
            }
        }
        return 0;
    }

    public boolean isGameRoundComplete() {
        return playerRound == gameState.getPlayers().size() - 1;
    }
}