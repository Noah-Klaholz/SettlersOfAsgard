package ch.unibas.dmi.dbis.cs108.shared.entities.Findables;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Monument entity which represents a fixed structure that is created upon initializing the board
 */
public class Monument extends FindableEntity {
    public static class Coordinates {
        public int x;
        public int y;
        public Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    /**
     * This represents how many runes this Entity generates for its owner
     */
    private int runes;

    /**
     * This represents if the Entity is part of a set, for which its owner should receive a setBonus
     */
    private boolean setBonus;

    /**
     * The path to the image representing this entity on the map.
     */
    protected String mapImagePath;

    /**
     * Indicates whether this entity has been disabled last turn.
     * Value above 0 means it was disabled
     * Value of 0 means it was not
     */
    protected int disabled = 0;

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
    public Monument() {}

    /**
     * Constructs a new Monument with specified values.
     *
     * @param id The unique identifier for this artifact
     * @param name The name of this artifact
     * @param description The description of this artifact
     * @param runes how many runes this entity farms each round
     * @param setBonus states wether this entity is part of a set
     */
    public Monument(int id, String name, String description, String usage, int runes, boolean setBonus, List<Coordinates> tiles, String world, String mapImagePath) {
        super(id, name, description, usage);
        this.runes = runes;
        this.setBonus = setBonus;
        this.tiles = tiles;
        this.world = world;
        this.mapImagePath = mapImagePath;
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
     * Sets the world of this Monument
     *
     * @param world The world of this monument
     */
    public void setWorld(String world) {
        this.world = world;
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
     * Gets the path to the image representing this entity as a game piece.
     *
     * @return The path to the image
     */
    public String getMapImagePath() {
        return mapImagePath;
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
     * Gets the world of this monument
     *
     * @return The world of this monument
     */
    public String getWorld() {
        return world;
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
     * Gets if this Monument is part of a set
     *
     * @return The set of this Monument
     */
    public boolean isSet() {
        return setBonus;
    }

    /**
     * Sets the mapImagePath of this Monument
     *
     * @param mapImagePath The path to the image representing this entity as a game piece
     */
    public void setMapImagePath(String mapImagePath) {
        this.mapImagePath = mapImagePath;
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
     * Loads Monument data from a JSON object.
     * Extends the parent method to also load Monument-specific data.
     *
     * @param json The JSON object containing Monument data
     */
    @Override
    protected void loadFromJson(JsonObject json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Coordinates>>() {}.getType();
        super.loadFromJson(json);
        this.runes = json.get("runes").getAsInt();
        this.world = json.get("world").getAsString();
        this.mapImagePath = json.get("mapImagePath").getAsString();
        JsonArray jArr = json.get("tiles").getAsJsonArray();
        this.tiles = gson.fromJson(jArr, listType);
        this.setBonus = tiles.size() > 1;
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
     * Returns a clone of this Monument.
     * This method creates a new instance of the Monument with the same properties as the original.
     *
     * @return A new Monument object that is a clone of this one
     */
    @Override
    public Monument clone() {
        Monument clone = new Monument();

        clone.setRunes(this.runes);
        clone.setWorld(this.world);
        clone.setMapImagePath(this.mapImagePath);
        clone.setTiles(new ArrayList<>(this.tiles));

        return (Monument) copyTo(clone);
    }
}