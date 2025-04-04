package ch.unibas.dmi.dbis.cs108.client.core.state;
import ch.unibas.dmi.dbis.cs108.client.core.entities.*;

public class GameState {

    private final Player player;
    private final Shop shop;
    private final Board board;
    private GameRound gameRound;
    private PlayerRound playerRound;

    public GameState(Player player, Shop shop) {
        this.player = player;
        this.shop = shop;
        this.board = new Board();
        this.gameRound = new GameRound();
        this.playerRound = new PlayerRound();
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
    public GameRound getGameRound() {
        return gameRound;
    }
    public PlayerRound getPlayerRound() {
        return playerRound;
    }
    public void setGameRound(GameRound gameRound) {
        this.gameRound = gameRound;
    }
    public void setPlayerRound(PlayerRound playerRound) {
        this.playerRound = playerRound;
    }


    //Methods that a Player uses
    public void buyTile(int x, int y) {
        // Logic to buy a tile
    }
    public void buyStatue(int x, int y) {
        // Logic to buy a statue
    }
    public void buyStructure(int x, int y) {
        // Logic to buy a structure
    }
    public void placeStatue(int x, int y, String statueID) {
        // Logic to place a statue
    }
    public void placeStructure(int x, int y, String structureID) {
        // Logic to place a structure
    }
    public void useStructure(int x, int y, String structureID, String useType) {
        // Logic to use a structure
    }
    public void upgradeStatue(int x, int y, String statueID) {
        // Logic to upgrade a statue
    }
    public void useStatue(int x, int y, String statueID, String useType) {
        // Logic to use a statue
    }
    public void useFieldArtifact(int x, int y, int artifactID, String useType) {
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
