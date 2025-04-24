package ch.unibas.dmi.dbis.cs108.client.core;

import ch.unibas.dmi.dbis.cs108.shared.game.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Player class is responsible for creating a player object
 */
public class Player {
    private String name;
    private int runes;
    private int energy;
    private List<Tile> ownedTiles;

    /**
     * Constructor for Player class
     * <p>
     * Player name must be unique and non-null.
     *
     * @param name String - The unique name for the player.
     */
    public Player(String name) {
        this.name = Objects.requireNonNull(name, "Player name cannot be null");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be empty or just whitespace.");
        }
        runes = 20;
        energy = 0;
        ownedTiles = new ArrayList<>();
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
     * Setter for name. Should ideally only be called based on server confirmation.
     * Ensures the name is not null or empty.
     *
     * @param name String - The new unique name for the player.
     */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Player name cannot be null");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be empty or just whitespace.");
        }
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
     * Setter for runes. Ensures runes don't go below 0.
     *
     * @param runes int
     */
    public void setRunes(int runes) {
        this.runes = Math.max(0, runes);
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
     * Setter for energy. Ensures energy doesn't go below 0.
     *
     * @param energy int
     */
    public void setEnergy(int energy) {
        this.energy = Math.max(0, energy);
    }

    /**
     * Getter for owned tiles. Returns a copy to prevent external modification.
     *
     * @return A new list containing the owned tiles.
     */
    public List<Tile> getOwnedTiles() {
        return new ArrayList<>(ownedTiles);
    }

    /**
     * Sets the owned tiles. Use with caution, prefer addOwnedTile/removeOwnedTile.
     *
     * @param ownedTiles List of Tiles
     */
    public void setOwnedTiles(List<Tile> ownedTiles) {
        this.ownedTiles = (ownedTiles != null) ? new ArrayList<>(ownedTiles) : new ArrayList<>();
    }

    /**
     * Adds runes to the player's total.
     *
     * @param amount The amount to add (can be negative).
     */
    public void addRunes(int amount) {
        setRunes(this.runes + amount);
    }

    /**
     * Adds energy to the player's total.
     *
     * @param amount The amount to add (can be negative).
     */
    public void addEnergy(int amount) {
        setEnergy(this.energy + amount);
    }

    /**
     * Adds a tile to the player's owned tiles if not already present.
     *
     * @param tile The tile to add.
     */
    public void addOwnedTile(Tile tile) {
        if (tile != null && !ownedTiles.contains(tile)) {
            ownedTiles.add(tile);
        }
    }

    /**
     * Removes a tile from the player's owned tiles.
     *
     * @param tile The tile to remove.
     * @return true if the tile was removed, false otherwise.
     */
    public boolean removeOwnedTile(Tile tile) {
        return ownedTiles.remove(tile);
    }

    /**
     * Returns a string representation of the Player object.
     *
     * @return A string containing the player's name, runes, energy, and number of owned tiles.
     */
    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", runes=" + runes +
                ", energy=" + energy +
                ", ownedTiles=" + ownedTiles.size() +
                '}';
    }

    /**
     * Compares this Player object with another for equality.
     *
     * @param o The object to compare with.
     * @return true if the names are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Player player = (Player) o;
        return name.equals(player.name);
    }

    /**
     * Returns a hash code value for the Player object.
     *
     * @return An integer hash code based on the player's name.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}