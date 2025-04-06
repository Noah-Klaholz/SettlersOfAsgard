package ch.unibas.dmi.dbis.cs108.server.core.entities;

import ch.unibas.dmi.dbis.cs108.server.core.entities.Artefact;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Player class is responsible for creating a player object
 */
public class Player {
    //private final String id;
    private final UUID playerID;
    private String name;
    private int runes;
    private int energy;
    private List<Tile> ownedTiles;
    private List<Artefact> artifacts;

    /**
     * Constructor for Player class
     * <p>
     * //@param id   String
     *
     * @param name String
     */
    public Player(String name) {
        this.playerID = UUID.randomUUID();
        this.name = name;
        runes = 20;
        energy = 0;
        ownedTiles = new ArrayList<>();
        artifacts = new ArrayList<>();
    }

    /**
     * Setter for artifacts
     *
     * @param artifacts List<Artefact>
     */
    public void setArtifacts(List<Artefact> artifacts) {
        this.artifacts = artifacts;
    }

    /**
     * Getter for artifacts
     *
     * @return List<Artefact>
     */
    public List<Artefact> getArtifacts() {
        return artifacts;
    }

    /**
     * adds an artifact to the player
     *
     * @param artifact Artefact
     */
    //should be checked in gameLogic if possible
    public void addArtifact(Artefact artifact) {
        artifacts.add(artifact);
    }

    /**
     * removes an artifact from the player
     *
     * @param artifact Artefact
     */
    public void removeArtifact(Artefact artifact) {
        artifacts.remove(artifact);
    }


    /**
     *
     * Getter for id
     *
     * @return String
     */
    public UUID getId() {
        return playerID;
    }

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
        return playerID;
    }

    /**
     * Getter for runes
     *
     * @return int
     */
    public int getRunes() {
        return runes;
    }

    /**
     * Setter for runes
     *
     * @param runes int
     */
    public void setRunes(int runes) {
        this.runes = runes;
    }

    /**
     * Getter for energy
     *
     * @return int
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * Setter for energy
     *
     * @param energy int
     */
    public void setEnergy(int energy) {
        this.energy = energy;
    }

    /**
     * Getter for player id
     *
     * @return player_id
     */
    public UUID getPlayerID() {
        return playerID;
    }

    /**
     * Getter for owned tiles
     *
     * @return List<Tile>
     */
    public List<Tile> getOwnedTiles() {
        return ownedTiles;
    }

    /**
     * Setter for owned tiles
     *
     * @param ownedTiles List<Tile>
     */
    public void setOwnedTiles(List<Tile> ownedTiles) {
        this.ownedTiles = ownedTiles;
    }

    /**
     * Adds runes to the player
     *
     * @param amount int
     */
    public void addRunes(int amount) {
        this.runes += amount;
    }

    /**
     * Adds energy to the player
     *
     * @param amount int
     */
    public void addEnergy(int amount) {
        this.energy += amount;
    }

    /**
     * adds a tile to the player
     *
     * @param tile Tile
     */
    public void addOwnedTile(Tile tile) {
        ownedTiles.add(tile);
    }

    /**
     * toString method for Player class
     *
     * @return String including name, runes and energy
     */
    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", runes=" + runes +
                ", energy=" + energy +
                '}';
    }

}
