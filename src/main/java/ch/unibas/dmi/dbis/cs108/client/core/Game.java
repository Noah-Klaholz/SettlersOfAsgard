package ch.unibas.dmi.dbis.cs108.client.core;
import ch.unibas.dmi.dbis.cs108.client.core.entities.*;

import java.util.ArrayList;

public class Game {

    private final Player player;
    private ArrayList<Player> players;
    private final Shop shop;
    private final Board board;

    public Game(Player player, Shop shop) {
        this.player = player;
        this.shop = shop;
        this.board = new Board();
        this.players = new ArrayList<>();
        //todo: get players from lobby from server
    }

    public Player getPlayer() {
        return player;
    }
    public Shop getShop() {
        return shop;
    }
    public Board getBoard() {
        return board;
    }


    //Methods that a Player uses
    public void buyTile(int x, int y) {
        // Greift aufs board zu
    }
    public void buyStatue(String statueID) {
        // Logic to buy a statue
    }
    public void buyStructure(String structureID) {
        // Logic to buy a structure
    }
    public void placeStatue(int x, int y, String statueID) {
        // needs to check if it is the right world -> implement all comparisons here (if statue.name = x and if tile.world = x then it works
    }
    public void placeStructure(int x, int y, String structureID) {
        // Logic to place a structure
    }
    public void useStructure(String structureID) {
        // Logic to use a structure
    }
    public void upgradeStatue(String statueID) {
        // Logic to upgrade a statue
    }
    public void useStatue(String statueID) {
        // Logic to use a statue
    }
    public void useFieldArtifact(int x, int y, int artifactID) {
        // Logic to use a field artifact
    }
    public void usePlayerArtifact(int artifactID, String useType) {
        // Logic to use a player artifact
    }
    public void endTurn() {
        // Logic to end the turn
    }
    public void startTurn() {
        // Logic to start the turn
    }

}
