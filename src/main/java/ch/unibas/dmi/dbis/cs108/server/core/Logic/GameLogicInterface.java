package ch.unibas.dmi.dbis.cs108.server.core.Logic;

/**
 * Interface for the GameLogic
 * Contains all the methods that are needed to implement the game logic
 * The GameLogic class implements this interface
 */
public interface GameLogicInterface {
    void startGame(); // Starts the game

    void endGame(); // Ends the game

    void startTurn(String playerID); // Starts the turn of a player

    void endTurn(String playerID); // Ends the turn of a player

    void buyTile(int x, int y, String playerID); // Buys a tile

    void placeStructure(int x, int y, String structureID, String playerID); // Works as buy and place function for Structures

    void useStructure(int x, int y, String structureID, String useType, String playerID); // Uses a structure

    void upgradeStatue(int x, int y, String statueID, String playerID); // Upgrades a statue

    void useStatue(int x, int y, String statueID, String useType, String playerID); // Uses a statue

    void useFieldArtifact(int x, int y, int artifactID, String useType); // Uses a field artifact

    void usePlayerArtifact(int artifactID, String playerID, String useType); // Uses a player artifact

    void useTrap(int x, int y, String trapID, String playerID); // Uses a trap

}
