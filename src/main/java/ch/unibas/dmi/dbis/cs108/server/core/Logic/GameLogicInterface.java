package ch.unibas.dmi.dbis.cs108.server.core.Logic;

/**
 * Interface for the GameLogic
 * Contains all the methods that are needed to implement the game logic
 * The GameLogic class implements this interface
 */
public interface GameLogicInterface {
    public void startGame(String[] players); // Starts the game
    public void endGame(); // Ends the game
    public void nextTurn(); // Starts the turn of a player
    public void endTurn(); // Ends the turn of a player
    public boolean buyTile(int x, int y, String playerID); // Buys a tile
    public void buyStatue(String statueID, String playerID); // Buys a statue
    public void buyStructure(String structureID, String playerID); // Buys a structure
    public boolean placeStructure(int x, int y, int structureID, String playerID); // Works as buy and place function for Structures
    public boolean useStructure(int x, int y, int structureID, String useType, String playerName); // Uses a structure
    public boolean upgradeStatue(int x, int y, String statueID, String playerName); // Upgrades a statue
    public void useStatue(int x, int y, int statueID, String useType, String playerName); // Uses a statue
    public void useFieldArtifact(int x, int y, int artifactID, String useType, String playerName); // Uses a field artifact
    public void usePlayerArtifact(int artifactID, String playerName, String useType, String playerAimedAt); // Uses a player artifact

}