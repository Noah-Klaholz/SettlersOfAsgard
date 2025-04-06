package ch.unibas.dmi.dbis.cs108.server.core.Logic;

import ch.unibas.dmi.dbis.cs108.server.core.State.GameState;
import ch.unibas.dmi.dbis.cs108.server.core.entities.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of the GameLogicInterface that provides the core game logic functionality.
 * This class handles all game mechanics including game flow, player turns, and player actions
 * such as buying tiles, placing structures, and using artifacts.
 */
public class GameLogic implements GameLogicInterface {

    private GameState gameState;

    public GameLogic(String[] players) {
        startGame(players);
    }

    /**
     * Initializes and starts a new game.
     * This method sets up the initial game state, including player order,
     * game field configuration, and starting resources.
     */
    @Override
    public void startGame(String[] players) {
        gameState = new GameState();
        gameState.setPlayers(players);
    }

    /**
     * Finalizes and ends the current game.
     * This method calculates final scores, determines the winner,
     * and cleans up game resources.
     */
    @Override
    public void endGame() {
        ArrayList<Player> players = gameState.getPlayerList();
        sortPlayersByScore(players);
        //todo: later: give players with same scores the same place
        String place1st = players.get(0).getName();
        String place2nd = players.get(1).getName();
        String place3rd = players.get(2).getName();
        int i = 1;
        for(Player player : players) {
            System.out.println("#" + i + ": " + player.getName() + " has " + player.getRunes() + " runes.");
            i++;
        }
        //todo: cleanup game resources
    }

    /**
     * Sorts the players based on their scores (runes).
     * This method sorts the players in descending order based on their runes.
     *
     * @param players List of players to be sorted
     */
    public void sortPlayersByScore(List<Player> players) {
        Collections.sort(players, Comparator.comparingInt(Player::getRunes).reversed());
    }

    /**
     * Begins a player's turn.
     * This method initializes the turn state, provides the player with turn-based
     * resources, and enables their ability to perform actions.
     *
     * @param playerID The unique identifier of the player whose turn is starting
     */
    /**
     * Advances to the next player's turn automatically.
     * Handles all turn initialization and round progression.
     */
    public void nextTurn() {
        // If first turn of game, initialize
        if (gameState.getPlayerTurn() == null) {
            initializeFirstTurn();
            return;
        }

        int nextPosition = (gameState.getPlayerRound() + 1) % gameState.getPlayerList().size();
        boolean newRound = nextPosition == 0;

        if (newRound) {
            gameState.setGameRound(gameState.getGameRound() + 1);

            // Check for game end condition (after 5 rounds)
            if (gameState.getGameRound() > 5) {
                endGame();
                return;
            }
        }

        gameState.setPlayerRound(nextPosition);
        Player nextPlayer = gameState.getPlayerList().get(nextPosition);
        gameState.setPlayerTurn(nextPlayer.getName());

    }

    private void initializeFirstTurn() {
        List<Player> players = gameState.getPlayerList();

        gameState.setGameRound(1);
        gameState.setPlayerRound(0);
        Player firstPlayer = gameState.getPlayerList().get(0);
        gameState.setPlayerTurn(firstPlayer.getName());
        gameState.addRunes(1, firstPlayer.getName());

    }

    /**
     * Concludes a player's turn.
     * This method finalizes any pending actions, applies end-of-turn effects,
     * and transitions to the next player's turn.
     *
     */
    @Override
    public void endTurn() {
        if (gameState.getPlayerTurn() == null) {
            throw new IllegalStateException("No active turn to end");
        }

        nextTurn(); // Advance to next player
    }

    /**
     * Processes a player's request to buy a tile at the specified coordinates.
     * Validates the purchase against game rules and updates the game state
     * if the purchase is successful.
     *
     * @param x The x-coordinate of the tile to purchase
     * @param y The y-coordinate of the tile to purchase
     * @param playerID The unique identifier of the player attempting to buy the tile
     */
    @Override
    public void buyTile(int x, int y, String playerID) {

    }

    /**
     * Handles a player's request to place a structure at the specified coordinates.
     * This method verifies the placement is valid according to game rules,
     * deducts the required resources, and updates the game state with the new structure.
     *
     * @param x The x-coordinate where the structure will be placed
     * @param y The y-coordinate where the structure will be placed
     * @param structureID The identifier of the structure to place
     * @param playerID The unique identifier of the player placing the structure
     */
    @Override
    public void placeStructure(int x, int y, String structureID, String playerID) {

    }

    /**
     * Processes a player's request to use a structure at the specified coordinates.
     * This method validates the action, applies the structure's effects based on the use type,
     * and updates the game state accordingly.
     *
     * @param x The x-coordinate of the structure to use
     * @param y The y-coordinate of the structure to use
     * @param structureID The identifier of the structure to use
     * @param useType The specific way the structure should be used
     * @param playerID The unique identifier of the player using the structure
     */
    @Override
    public void useStructure(int x, int y, String structureID, String useType, String playerID) {

    }

    /**
     * Handles a player's request to upgrade a statue at the specified coordinates.
     * This method verifies the upgrade is valid, deducts the required resources,
     * and enhances the statue's capabilities.
     *
     * @param x The x-coordinate of the statue to upgrade
     * @param y The y-coordinate of the statue to upgrade
     * @param statueID The identifier of the statue to upgrade
     * @param playerID The unique identifier of the player upgrading the statue
     */
    @Override
    public void upgradeStatue(int x, int y, String statueID, String playerID) {

    }

    /**
     * Processes a player's request to use a statue at the specified coordinates.
     * This method validates the action, applies the statue's effects based on the use type,
     * and updates the game state accordingly.
     *
     * @param x The x-coordinate of the statue to use
     * @param y The y-coordinate of the statue to use
     * @param statueID The identifier of the statue to use
     * @param useType The specific way the statue should be used
     * @param playerID The unique identifier of the player using the statue
     */
    @Override
    public void useStatue(int x, int y, String statueID, String useType, String playerID) {

    }

    /**
     * Handles the activation of an artifact that affects the game field.
     * This method applies the artifact's effects to the specified location
     * and updates the game state accordingly.
     *
     * @param x The x-coordinate where the field artifact will be used
     * @param y The y-coordinate where the field artifact will be used
     * @param artifactID The identifier of the artifact to use
     */
    @Override
    public void useFieldArtifact(int x, int y, int artifactID, String useType) {

    }

    /**
     * Processes the activation of an artifact that directly affects a player.
     * This method applies the artifact's effects to the specified player
     * and updates the game state accordingly.
     *
     * @param artifactID The identifier of the artifact to use
     * @param playerID The unique identifier of the player who will be affected
     */
    @Override
    public void usePlayerArtifact(int artifactID, String playerID, String useType) {

    }

    public void buyStatue(String statueID, String playerID) {
        // Implementation for buying a statue
    }

    public void buyStructure(String structureID, String playerID) {
        // Implementation for buying a structure
    }

}