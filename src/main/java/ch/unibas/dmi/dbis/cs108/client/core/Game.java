package ch.unibas.dmi.dbis.cs108.client.core;

import ch.unibas.dmi.dbis.cs108.shared.entities.Board;
import ch.unibas.dmi.dbis.cs108.shared.entities.Player;
import ch.unibas.dmi.dbis.cs108.shared.entities.Shop;

import java.util.ArrayList;

/**
 * The Game class represents the main game logic and state.
 * It manages the player, shop, board, and provides methods for game actions.
 */
public class Game {

    /**
     * The player associated with this game instance.
     */
    private final Player player;
    /**
     * The shop associated with this game instance.
     */
    private final Shop shop;
    /**
     * The board associated with this game instance.
     */
    private final Board board;
    /**
     * The list of players in the game.
     */
    private ArrayList<Player> players;

    /**
     * Constructor for the Game class.
     *
     * @param player The player associated with this game instance.
     * @param shop   The shop associated with this game instance.
     */
    public Game(Player player, Shop shop) {
        this.player = player;
        this.shop = shop;
        this.board = new Board();
        this.players = new ArrayList<>();
        //todo: get players from lobby from server
    }

    /**
     * Returns the Player object associated with this game instance.
     * @return The Player object.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the Shop object associated with this game instance.
     * @return The Shop object.
     */
    public Shop getShop() {
        return shop;
    }

    /**
     * Returns the board in the game.
     * @return the board object.
     */
    public Board getBoard() {
        return board;
    }


    //Methods that a Player uses

    /**
     * Buys a tile at the specified coordinates.
     * @param x the x-coordinate of the tile
     * @param y the y-coordinate of the tile
     */
    public void buyTile(int x, int y) {
        // Greift aufs board zu
    }

    /**
     * Buys a statue with the specified ID.
     * @param statueID the ID of the statue to buy
     */
    public void buyStatue(String statueID) {
        // Logic to buy a statue
    }

    /**
     * Buys a structure with the specified ID.
     * @param structureID the ID of the structure to buy
     */
    public void buyStructure(String structureID) {
        // Logic to buy a structure
    }

    /**
     * Places a statue at the specified coordinates.
     * @param x the x-coordinate to place the statue
     * @param y the y-coordinate to place the statue
     * @param statueID the ID of the statue to place
     */
    public void placeStatue(int x, int y, String statueID) {
        // needs to check if it is the right world -> implement all comparisons here (if statue.name = x and if tile.world = x then it works
    }

    /**
     * Places a structure at the specified coordinates.
     * @param x the x-coordinate to place the structure
     * @param y the y-coordinate to place the structure
     * @param structureID the ID of the structure to place
     */
    public void placeStructure(int x, int y, String structureID) {
        // Logic to place a structure
    }

    /**
     * Uses a structure with the specified ID.
     * @param structureID the ID of the structure to use
     */
    public void useStructure(String structureID) {
        // Logic to use a structure
    }

    /**
     * Upgrades a statue with the specified ID.
     * @param statueID the ID of the statue to upgrade
     */
    public void upgradeStatue(String statueID) {
        // Logic to upgrade a statue
    }

    /**
     * Uses a statue with the specified ID.
     * @param statueID the ID of the statue to use
     */
    public void useStatue(String statueID) {
        // Logic to use a statue
    }

    /**
     * Uses a field artifact at the specified coordinates.
     * @param x the x-coordinate of the artifact
     * @param y the y-coordinate of the artifact
     * @param artifactID the ID of the artifact to use
     */
    public void useTileArtifact(int x, int y, int artifactID) {
        // Logic to use a field artifact
    }

    /**
     * Uses a player artifact with the specified ID.
     * @param artifactID the ID of the artifact to use
     * @param useType the type of use for the artifact
     */
    public void usePlayerArtifact(int artifactID, String useType) {
        // Logic to use a player artifact
    }

    /**
     * Ends the current turn.
     */
    public void endTurn() {
        // Logic to end the turn
    }

    /**
     * Starts the current turn.
     */
    public void startTurn() {
        // Logic to start the turn
    }

    /**
     * Updates the Player name to the new name.
     *
     * @param newName The new name for the player.
     */
    public void updatePlayerName(String newName) {
        player.setName(newName);
        // Todo: Notify the server about the name change
    }

}
