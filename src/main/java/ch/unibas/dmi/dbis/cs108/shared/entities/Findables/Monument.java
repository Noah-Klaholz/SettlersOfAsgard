package ch.unibas.dmi.dbis.cs108.shared.entities.Findables;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Monument entity which represents a fixed structure that is created upon initializing the board
 */
public class Monument extends FindableEntity {

    /**
     * Indicates whether this entity has been disabled last turn.
     * Value above 0 means it was disabled
     * Value of 0 means it was not
     */
    protected int disabled = 0;
    /**
     * String representing the URL for the map-image
     */
    String mapImagePath;
    /**
     * This represents how many runes this Entity generates for its owner
     */
    private int runes;
    /**
     * This represents if the Entity is part of a set, for which its owner should receive a setBonus
     */
    private boolean setBonus;
    /**
     * A List representing the Tiles this Monument is placed upon
     */
    private List<Coordinates> tiles;

    /**
     * String representaing the name of the world
     */
    private String world;
    /**
     * Default constructor for Monument.
     */
    public Monument() {
    }

    /**
     * Constructs a new Monument with specified values.
     *
     * @param id           The unique identifier for this artifact
     * @param name         The name of this artifact
     * @param description  The description of this artifact
     * @param runes        how many runes this entity farms each round
     * @param setBonus     states wether this entity is part of a set
     * @param mapImagePath the URl for the image to be drawn on the map
     * @param tiles       The tiles this artifact is placed upon
     *  @param world        The name of the world
     *  @param usage        The usage of this artifact
     */
    public Monument(int id, String name, String description, String usage, int runes, boolean setBonus, List<Coordinates> tiles, String world, String mapImagePath) {

        /*
         * Call the parent constructor to initialize common properties
         * such as id, name, description, and usage.
         */
        super(id, name, description, usage);
        /*
         * runes is the number of runes this entity generates for its owner
         */
        this.runes = runes;
        /*
         * setBonus is a boolean that indicates if this entity is part of a set
         */
        this.setBonus = setBonus;
        /*
         * tiles is a List of Coordinates that represent the tiles this monument is placed upon
         */
        this.tiles = tiles;
        /*
         * world is a String that represents the name of the world
         */
        this.world = world;
        /*
         * mapImagePath is a String that represents the URL for the map-image
         */
        this.mapImagePath = mapImagePath;
    }

    /**
     * Factory method to create a Monument from JSON data.
     *
     * @param json The JSON object containing artifact data
     * @return A new Monument instance populated with data from the JSON
     */
    public static Monument fromJson(JsonObject json) {
        Monument artifact = new Monument();
        artifact.loadFromJson(json);
        return artifact;
    }

    /**
     * Gets the runes of this Monument.
     *
     * @return The runes this monument produces
     */
    public int getRunes() {
        return runes;
    }

    /**
     * Sets the runes of this Monument.
     *
     * @param runes The runes this monument produces
     */
    public void setRunes(int runes) {
        this.runes = runes;
    }

    /**
     * Gets the world of this monument
     *
     * @return The world of this monument
     */
    public String getWorld() {
        return world;
    }

    /**
     * Sets the world of this Monument
     *
     * @param world The world of this monument
     */
    public void setWorld(String world) {
        this.world = world;
    }

    /**
     * Gets the Tiles this Monument spans over as a List
     *
     * @return tiles
     */
    public List<Coordinates> getTiles() {
        return tiles;
    }

    /**
     * Sets the Tiles this Monument spans over
     *
     * @param tiles The tiles this monument spans over
     */
    public void setTiles(List<Coordinates> tiles) {
        this.tiles = tiles;
    }

    /**
     * Gets if this Monument is part of a set
     *
     * @return The set of this Monument
     */
    public boolean isSet() {
        return setBonus;
    }

    /**
     * Gets if this Monument is part of a set
     *
     * @return The set of this Monument
     */
    public int getSetBonus() {
        return (runes * SETTINGS.Config.SET_BONUS_MULTIPLIER.getValue());
    }

    /**
     * Returns if this Monument is currently disabled,
     * Any value above 0 means yes
     *
     * @return true if yes, false otherwise
     */
    public boolean isDisabled() {
        return disabled > 0;
    }

    /**
     * Sets the value of disable.
     *
     * @param disabled the value to set.
     */
    public void setDisabled(int disabled) {
        this.disabled = disabled;
    }

    /**
     * Disables this Monument for "turns" number of turns
     *
     * @param turns the number of turns it should be disabled
     */
    public void disable(int turns) {
        this.disabled = turns;
    }

    /**
     * Ticks one turn off of the disabled "timer"
     */
    public void disabledTurn() {
        this.disabled--;
    }

    /**
     * Getter for the Map image path
     *
     * @return the image URL
     */
    public String getMapImagePath() {
        return mapImagePath;
    }

    /**
     * Sets the mapImagePath
     *
     * @param mapImagePath the value to set
     */
    public void setMapImagePath(String mapImagePath) {
        this.mapImagePath = mapImagePath;
    }

    /**
     * Loads Monument data from a JSON object.
     * Extends the parent method to also load Monument-specific data.
     *
     * @param json The JSON object containing Monument data
     */
    @Override
    protected void loadFromJson(JsonObject json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Coordinates>>() {
        }.getType();
        super.loadFromJson(json);
        this.runes = json.get("runes").getAsInt();
        this.world = json.get("world").getAsString();
        this.mapImagePath = json.get("mapImagePath").getAsString();
        JsonArray jArr = json.get("tiles").getAsJsonArray();
        this.tiles = gson.fromJson(jArr, listType);
        this.setBonus = tiles.size() > 1;
    }

    /**
     * Returns a clone of this Monument.
     * This method creates a new instance of the Monument with the same properties as the original.
     *
     * @return A new Monument object that is a clone of this one
     */
    @Override
    public Monument clone() {
        /*
         * Create a new instance of Monument
         * and copy the properties from this instance.
         */
        Monument clone = new Monument();

        /*
         * Copy properties from this instance to the clone
         * using the copyTo method.
         */
        clone.setRunes(this.runes);
        /*
         * Copy the world name
         */
        clone.setWorld(this.world);
        /*
         * Copy the disabled value
         */
        clone.setTiles(new ArrayList<>(this.tiles));
        /*
         * Copy the setBonus value
         */
        clone.setMapImagePath(this.mapImagePath);

        return (Monument) copyTo(clone);
    }

    /**
     * A class representing a pair of coordinates defined by x and y values.
     * This class is immutable and provides an implementation for equals and hashCode
     * methods, making it suitable for use in collections that rely on these methods.
     */
    public static class Coordinates {
        /**
         * The x-coordinate of the tile
         */
        public final int x;
        /**
         * The y-coordinate of the tile
         */
        public final int y;

        /**
         * Constructor for Coordinates
         *
         * @param x The x-coordinate of the tile
         * @param y The y-coordinate of the tile
         */
        public Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Factory method to create a Coordinates object from JSON data.
         *
         *
         * @return A new Coordinates instance populated with data from the JSON
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Coordinates other)) return false;
            return this.x == other.x && this.y == other.y;
        }

        /**
         * Returns a hash code value for this Coordinates object.
         * The hash code is computed based on the x and y values.
         *
         * @return A hash code value for this Coordinates object
         */
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}