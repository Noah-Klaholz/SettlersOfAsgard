package ch.unibas.dmi.dbis.cs108.client.core.entities;

/**
 * Player class is responsible for creating a player object
 */
public class Player {
    //private final String id;
    //todo: add player id
    private String name;

    /**
     * Constructor for Player class
     *
     * //@param id   String
     * @param name String
     */
    public Player(String name) {
        //this.id = id;
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
}
