package ch.unibas.dmi.dbis.cs108.server.core.Logic;

import ch.unibas.dmi.dbis.cs108.client.core.entities.ActiveTrap;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Artifact;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Statue;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Structure;

/**
 * Interface for the GameLogic
 * Contains all the methods that are needed to implement the game logic
 * The GameLogic class implements this interface
 */
public interface GameLogicInterface {
    public void startGame(); // Starts the game
    public void endGame(); // Ends the game
    public void startTurn(String playerID); // Starts the turn of a player
    public void endTurn(String playerID); // Ends the turn of a player
    public void buyTile(int x, int y, String playerID); // Buys a tile
    public void buyStatue(String statueID, String playerID); // Buys a statue
    public void buyStructure(String structureID, String playerID); // Buys a structure
    public void placeStructure(int x, int y, String structureID, String playerID); // Works as buy and place function for Structures
    public void useStructure(int x, int y, String structureID, String useType, String playerID); // Uses a structure
    public void upgradeStatue(int x, int y, String statueID, String playerID); // Upgrades a statue
    public void useStatue(int x, int y, String statueID, String useType, String playerID); // Uses a statue
    public void useTileArtifact(int x, int y, int artifactID, String useType); // Uses a field artifact
    public void usePlayerArtifact(int artifactID, String playerID, String useType); // Uses a player artifact

}
