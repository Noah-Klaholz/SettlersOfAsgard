package ch.unibas.dmi.dbis.cs108.server.core.Logic;

import ch.unibas.dmi.dbis.cs108.server.core.State.GameState;
import ch.unibas.dmi.dbis.cs108.server.core.entities.*;

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
     * Gets the gamestate
     *
     * @return the gamestate
     */
    public GameState getGameState() {
        return gameState;
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
    public boolean buyTile(int x, int y, String playerName) {
        //todo: later implement check: can only purchase max. 3 tiles per turn
        if (gameState.getBoard().getTileByCoordinates(x, y) == null) {
            System.out.println("Tile not found");
            return false;
        }
        if (gameState.getBoard().getTileByCoordinates(x, y).isPurchased()) {
            System.out.println("Tile already purchased");
            return false;
        }
        for (Player player : gameState.getPlayerList()) {
            if (player.getName().equals(playerName)) {
                if (player.getRunes() >= gameState.getBoard().getTileByCoordinates(x, y).getPrice()) {
                    player.removeRunes(gameState.getBoard().getTileByCoordinates(x, y).getPrice());
                    gameState.getBoard().getTileByCoordinates(x, y).setPurchased(true);
                    player.addOwnedTile(gameState.getBoard().getTileByCoordinates(x, y));
                    System.out.println("Tile purchased");
                    return true;
                } else {
                    System.out.println("Not enough runes");
                    return false;
                }
            }
        }
        System.out.println("something went wrong");
        return false;
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
    public boolean placeStructure(int x, int y, int structureID, String playerName) {
        for (Player player : gameState.getPlayerList()) {
            if (player.getName().equals(playerName)) {
                for (Structure structure : player.getOwnedStructures()) {
                    if (structure.getStructureID() == structureID) {
                        if (gameState.getBoard().getTileByCoordinates(x, y) == null) {
                            System.out.println("Tile not found");
                            return false;
                        }
                        for (Tile tile : player.getOwnedTiles()) {
                            if (tile.getTileID() == gameState.getBoard().getTileByCoordinates(x, y).getTileID()) {
                                if (!tile.getHasStructure()) {
                                    player.removeOwnedStructure(structure);
                                    tile.setHasStructure(true);
                                    tile.setStructure(structure);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Could not place structure.");
        return false;
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
    public boolean useStructure(int x, int y, int structureID, String useType, String playerName) {
        for (Player player : gameState.getPlayerList()) {
            if (player.getName().equals(playerName)) {
                for (Structure structure : player.getOwnedStructures()) {
                    if (structure.getStructureID() == structureID) {
                        //player owns the structure
                        //todo: later implement 1 time use per turn
                        //todo: later implement structure use
                        return true;
                    }
                }
            }
        }
        return false;
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
    public boolean upgradeStatue(int x, int y, String statueID, String playerName) {
        for (Player player : gameState.getPlayerList()) {
            if (player.getName().equals(playerName)) {
                if (player.getStatue().getName().equals(statueID)) {
                    //player owns the statue
                    if (player.getRunes() >= player.getStatue().getUpgradePrice()) {
                        player.removeRunes(player.getStatue().getUpgradePrice());
                        player.getStatue().upgrade();
                        //todo: later check if it does actually upgrade (it gives a boolean back)
                        System.out.println("Statue upgraded");
                        return true;
                    } else {
                        System.out.println("Not enough runes");
                        return false;
                    }
                } else {
                    System.out.println("Player does not own the statue");
                    return false;
                }
            }
        }
        System.out.println("something went wrong");
        return false;
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
    public void useStatue(int x, int y, int statueID, String useType, String playerName) {
        for (Player player : gameState.getPlayerList()) {
            if (player.getName().equals(playerName)) {
                if (player.getStatue().getStatueID() == statueID) {
                    player.getStatue().use();
                }
            }
        }
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
    public void useFieldArtifact(int x, int y, int artifactID, String useType, String playerName) {
        for (Player player : gameState.getPlayerList()) {
            if (player.getName().equals(playerName)) {
                for (Artefact artefact : player.getArtifacts()) {
                    if (artefact.getArtifactID() == artifactID) {
                        if (gameState.getBoard().getTileByCoordinates(x, y) == null) {
                            System.out.println("Tile not found");
                            return;
                        }
                        //todo: implement artifact effect
                    }
                }
            }
        }

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
    public void usePlayerArtifact(int artifactID, String playerName, String useType, String playerAimedAt) {
        for (Player player : gameState.getPlayerList()) {
            if (player.getName().equals(playerName)) {
                for (Artefact artefact : player.getArtifacts()) {
                    if (artefact.getArtifactID() == artifactID) {
                        //player owns the artifact
                        for (Player otherPlayer : gameState.getPlayerList()) {
                            if (otherPlayer.getName().equals(playerName)) {
                                //todo: later implement use of player artifact
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void buyStatue(String statueID, String playerName) {
        Player targetPlayer = null;
        for (Player player : gameState.getPlayerList()) {
            if (player.getName().equals(playerName)) {
                targetPlayer = player;
                break;
            }
        }
        if (targetPlayer == null) {
            System.out.println("Player not found.");
            return;
        }

        if (targetPlayer.getStatue() != null) {
            System.out.println("Player already owns a statue.");
            return;
        }

        try {
            int id = Integer.parseInt(statueID);
            Shop shop = targetPlayer.getShop();
            Statue targetStatue = null;

            for (Statue statue : shop.getBuyableStatues()) {
                if (statue.getStatueID() == id) {
                    targetStatue = statue;
                    break;
                }
            }

            if (targetStatue == null) {
                System.out.println("Statue not found in shop.");
                return;
            }

            int cost = targetStatue.getPrice();
            if (targetPlayer.getRunes() >= cost) {
                targetPlayer.removeRunes(cost);
                targetPlayer.setStatue(targetStatue);
                shop.blockStatue();
                System.out.println("Statue purchased successfully.");
            } else {
                System.out.println("Insufficient runes to buy statue.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid statue ID provided.");
        }
    }

    public void buyStructure(String structureID, String playerName) {
        Player player = null;
        for (Player p : gameState.getPlayerList()) {
            if (p.getName().equals(playerName)) {
                player = p;
                break;
            }
        }

        if (player == null) {
            System.out.println("Player not found.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(structureID);
        } catch (NumberFormatException e) {
            System.out.println("Invalid structure ID provided.");
            return;
        }

        Shop shop = player.getShop();
        Structure targetStructure = null;

        for (Structure structure : shop.getBuyableStructures()) {
            if (structure.getStructureID() == id) {
                targetStructure = structure;
                break;
            }
        }

        if (targetStructure == null) {
            System.out.println("Structure not found in shop.");
            return;
        }

        // Check if player has enough runes
        int price = targetStructure.getPrice();
        if (player.getRunes() >= price) {
            player.removeRunes(price);
            player.addOwnedStructure(targetStructure);
            shop.removeStructure(targetStructure);
            System.out.println("Structure purchased successfully.");
        } else {
            System.out.println("Insufficient runes to buy structure.");
        }
    }

}