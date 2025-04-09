package ch.unibas.dmi.dbis.cs108.server.core.Logic;

/**
 * Interface for the GameLogic
 * Contains all the methods that are needed to implement the game logic
 * The GameLogic class implements this interface
 */
public interface GameLogicInterface {
    /**
     * Starts the game with the specified players.
     *
     * @param players Array of player identifiers
     */
    public void startGame(String[] players);

    /**
     * Ends the current game.
     */
    public void endGame();

    /**
     * Starts the turn of the next player.
     */
    public void nextTurn();

    /**
     * Ends the current player's turn.
     */
    public void endTurn();

    /**
     * Buys a tile at the specified coordinates for a player.
     *
     * @param x X-coordinate of the tile
     * @param y Y-coordinate of the tile
     * @param playerID Identifier of the player buying the tile
     * @return true if the purchase was successful, false otherwise
     */
    public boolean buyTile(int x, int y, String playerID);

    /**
     * Buys a statue for a player.
     *
     * @param statueID Identifier of the statue to buy
     * @param playerID Identifier of the player buying the statue
     */
    public boolean buyStatue(String statueID, String playerID);

    /**
     * Buys a structure for a player.
     *
     * @param structureID Identifier of the structure to buy
     * @param playerID Identifier of the player buying the structure
     */
    public boolean buyStructure(String structureID, String playerID);

    /**
     * Buys and places a structure at the specified coordinates for a player.
     *
     * @param x X-coordinate for structure placement
     * @param y Y-coordinate for structure placement
     * @param structureID Identifier of the structure to place
     * @param playerID Identifier of the player placing the structure
     * @return true if placement was successful, false otherwise
     */
    public boolean placeStructure(int x, int y, int structureID, String playerID);

    /**
     * Uses a structure at the specified coordinates.
     *
     * @param x X-coordinate of the structure
     * @param y Y-coordinate of the structure
     * @param structureID Identifier of the structure to use
     * @param useType Type of action to perform with the structure
     * @param playerName Name of the player using the structure
     * @return true if the structure was successfully used, false otherwise
     */
    public boolean useStructure(int x, int y, int structureID, String useType, String playerName);

    /**
     * Upgrades a statue at the specified coordinates.
     *
     * @param x X-coordinate of the statue
     * @param y Y-coordinate of the statue
     * @param statueID Identifier of the statue to upgrade
     * @param playerName Name of the player upgrading the statue
     * @return true if the upgrade was successful, false otherwise
     */
    public boolean upgradeStatue(int x, int y, String statueID, String playerName);

    /**
     * Uses a statue at the specified coordinates.
     *
     * @param x X-coordinate of the statue
     * @param y Y-coordinate of the statue
     * @param statueID Identifier of the statue to use
     * @param useType Type of action to perform with the statue
     * @param playerName Name of the player using the statue
     */
    public boolean useStatue(int x, int y, int statueID, String useType, String playerName);

    /**
     * Uses a field artifact at the specified coordinates.
     *
     * @param x X-coordinate of the field artifact
     * @param y Y-coordinate of the field artifact
     * @param artifactID Identifier of the artifact to use
     * @param useType Type of action to perform with the artifact
     * @param playerName Name of the player using the artifact
     */
    public boolean useFieldArtifact(int x, int y, int artifactID, String useType, String playerName);

    /**
     * Uses a player artifact.
     *
     * @param artifactID Identifier of the artifact to use
     * @param playerName Name of the player using the artifact
     * @param useType Type of action to perform with the artifact
     * @param playerAimedAt Name of the target player for the artifact effect
     */
    public boolean usePlayerArtifact(int artifactID, String playerName, String useType, String playerAimedAt);
}