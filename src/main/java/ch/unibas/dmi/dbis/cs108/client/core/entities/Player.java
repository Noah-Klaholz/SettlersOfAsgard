package ch.unibas.dmi.dbis.cs108.client.core.entities;

import java.util.UUID;

/**
 * Player class is responsible for creating a player object
 */
public class Player {
    //private final String id;
    private final UUID player_id;
    private String name;

    /**
     * Constructor for Player class
     * <p>
     * //@param id   String
     *
     * @param name String
     */
    public Player(String name) {
        this.player_id = UUID.randomUUID();
        this.name = name;
    }


    /**
     *
     * Getter for id
     *
     * @return String
     */
    /*public String getId() {
        return id;
    }*/

    /**
     * Getter for name
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name
     *
     * @param name String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for player id
     *
     * @return player_id
     */
    public UUID getPlayer_id() {
        return player_id;
    }

    public void buyTile(int x, int y, String playerID){

    }

    public void placeStructure(int x, int y, String structureID, String playerID){

    }

    public void useStructure(int x, int y, String structureID, String useType, String playerID){

    }

    public void upgradeStatue(int x, int y, String statueID, String playerID){

    }

    public void useStatue(int x, int y, String statueID, String useType, String playerID){

    }

    public void useFieldArtifact(int x, int y, int artifactID, String useType){

    }

    public void usePlayerArtifact(int artifactID, String playerID, String useType){

    }
}
