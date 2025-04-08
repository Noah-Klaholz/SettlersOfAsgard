package ch.unibas.dmi.dbis.cs108.server.core.Logic;

import ch.unibas.dmi.dbis.cs108.server.core.State.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.entities.*;

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
        resourcesIncome(nextPlayer);

    }

    /**
     * Provides resources to all players at the start of their turn.
     * This method distributes resources based on owned tiles and structures.
     *
     * Structures: A value below 5 gives energy, any above gives runes: that is how it is determined which one is given
     *
     */
    public void resourcesIncome(Player player){
        //Tile income (Runes)
        for(Tile tile : player.getOwnedTiles()) {
            player.addRunes(tile.getResourceValue());
        }
        //Structure income (Runes, Energy)
        for(Structure structure : player.getOwnedStructures()) {
            if(structure.getResourceValue() <= 4){
                player.addEnergy(structure.getResourceValue());
            }
            else {
                player.addRunes(structure.getResourceValue());
            }
        }
    }

    /**
     * Initializes the first turn of the game.
     * This method sets the initial player, assigns starting resources,
     * and prepares the game state for the first round.
     *
     */
    private void initializeFirstTurn() {
        List<Player> players = gameState.getPlayerList();

        gameState.setGameRound(1);
        gameState.setPlayerRound(0);
        Player firstPlayer = gameState.getPlayerList().get(0);
        gameState.setPlayerTurn(firstPlayer.getName());
        gameState.addRunes(1, firstPlayer.getName());
        //gameState.setActivePlayer(firstPlayer.getName());

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
     * @param playerName The unique identifier of the player attempting to buy the tile
     */
    @Override
    public boolean buyTile(int x, int y, String playerName) {
        Tile tile = gameState.getBoard().getTileByCoordinates(x, y);
        if (tile == null || tile.isPurchased()) {
            System.out.println(tile == null ? "Tile not found" : "Tile is already purchased");
            return false;
        }
        //todo: later implement check: can only purchase max. 3 tiles per turn
        Player player = findPlayerByName(playerName);
        if (player == null || player.getRunes() < tile.getPrice()) {
            System.out.println(player == null ? "Player not found" : "Not enough runes");
            return false;
        }

        player.removeRunes(tile.getPrice());
        tile.setPurchased(true);
        player.addOwnedTile(tile);
        System.out.println("Tile purchased");
        return true;
    }

    /**
     * Handles a player's request to place a structure at the specified coordinates.
     * This method verifies the placement is valid according to game rules,
     * deducts the required resources, and updates the game state with the new structure.
     *
     * @param x The x-coordinate where the structure will be placed
     * @param y The y-coordinate where the structure will be placed
     * @param structureID The identifier of the structure to place
     * @param playerName The unique identifier of the player placing the structure
     */
    @Override
    public boolean placeStructure(int x, int y, int structureID, String playerName) {
        Player player = findPlayerByName(playerName);
        if (player == null) {
            System.out.println("Player not found");
            return false;
        }
        Structure structure = player.getOwnedStructures().stream()
                .filter(s -> s.getStructureID() == structureID)
                .findFirst()
                .orElse(null);
        if (structure == null) {
            System.out.println("Structure not found");
            return false;
        }
        Tile tile = gameState.getBoard().getTileByCoordinates(x, y);
        if (tile == null || tile.isPurchased()) {
            System.out.println(tile == null ? "Tile not found" : "Tile is already purchased");
            return false;
        }
        if (tile.getHasStructure()) {
            System.out.println("Tile already has structure");
            return false;
        }

        player.removeOwnedStructure(structure);
        tile.setStructure(structure);
        tile.setHasStructure(true);
        return true;

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
     * @param playerName The unique identifier of the player using the structure
     */
    @Override
    public boolean useStructure(int x, int y, int structureID, String useType, String playerName) {
        Player player = findPlayerByName(playerName);
        if (player == null) {
            System.out.println("Player not found");
            return false;
        }
        Structure structure = player.getOwnedStructures().stream()
                .filter(s -> s.getStructureID() == structureID)
                .findFirst()
                .orElse(null);

        if(structure == null) {
            System.out.println("Structure not found");
            return false;
        }

        //todo: later implement 1 time use per turn
        // todo: later implement structure use
        return true;
    }

    /**
     * Handles a player's request to upgrade a statue at the specified coordinates.
     * This method verifies the upgrade is valid, deducts the required resources,
     * and enhances the statue's capabilities.
     *
     * @param x The x-coordinate of the statue to upgrade
     * @param y The y-coordinate of the statue to upgrade
     * @param statueID The identifier of the statue to upgrade
     * @param playerName The unique identifier of the player upgrading the statue
     */
    @Override
    public boolean upgradeStatue(int x, int y, String statueID, String playerName) {
        Player player = findPlayerByName(playerName);
        if (player == null || !player.getStatue().getName().equals(statueID)) {
            System.out.println(player == null ? "Player not found" : "Player does not own the statue");
            return false;
        }

        Statue statue = player.getStatue();
        if (player.getRunes() < statue.getPrice()) {
            System.out.println("Not enough runes");
            return false;
        }

        player.removeRunes(statue.getUpgradePrice());
        boolean upgraded = statue.upgrade();
        System.out.println(upgraded ? "Upgraded statue" : "Upgrade failed");
        return upgraded;

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
     * @param playerName The unique identifier of the player using the statue
     */
    @Override
    public void useStatue(int x, int y, int statueID, String useType, String playerName) {
        Player player = findPlayerByName(playerName);
        if (player == null) {
            System.out.println("Player not found");
            return;
        }
        Statue statue = player.getStatue();
        if (statue == null || statue.getStatueID() != statueID) {
            System.out.println(statue == null ? "Statue not found" : "Player does not own statue");
            return;
        }
        statue.use();
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
     * @param playerName The unique identifier of the player who will be affected
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

    /**
     * Gets the player Object by looking for the name.
     *
     * @param name the player's name
     * @return the Player Object corresponding to the name
     */
    private Player findPlayerByName(String name) {
        return gameState.getPlayerList().stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

}