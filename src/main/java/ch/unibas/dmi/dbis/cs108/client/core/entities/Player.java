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
}
