// TurnManager.java
package ch.unibas.dmi.dbis.cs108.server.core.logic;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
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
        // If first turn of game, initialize
        if (getPlayerTurn() == null) {
            initializeFirstTurn();
            return;
        }

        int nextPosition = (getPlayerRound() + 1) % gameState.getPlayers().size();
        boolean newRound = nextPosition == 0;

        if (newRound) {
            setGameRound(getGameRound() + 1);
        }

        setPlayerRound(nextPosition);
        Player nextPlayer = gameState.getPlayers().get(nextPosition);
        setPlayerTurn(nextPlayer.getName());

        // Distribute resources to the player starting their turn
        distributeResources(nextPlayer);

        // Notify clients about turn change
        if (communicationApi != null) {
            communicationApi.sendMessage("TURN$" + nextPlayer.getName());
        }
    }

    private void initializeFirstTurn() {
        // Start with a random player
        setPlayerRound(0);
        Player firstPlayer = gameState.getPlayers().get(0);
        setPlayerTurn(firstPlayer.getName());

        // Initial resources for first player
        distributeResources(firstPlayer);

        // Notify about first turn
        if (communicationApi != null) {
            communicationApi.sendMessage("GAME_START$First turn: " + firstPlayer.getName());
        }
    }

    public boolean endTurn(String playerName) {
        if (!playerName.equals(getPlayerTurn())) {
            return false;
        }

        nextTurn();
        return true;
    }

    public void reset() {
        gameRound = 0;
        playerRound = 0;
        playerTurn = null;
    }

    private void distributeResources(Player player) {
        // Tile income
        player.getOwnedTiles().forEach(tile ->
                player.addRunes(tile.getResourceValue())
        );
        // Structure income
        player.getOwnedStructures().forEach(structure -> {
            int value = structure.getRessourceValue();
            if (value <= 4) player.addEnergy(value);
            else player.addRunes(value);
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
}