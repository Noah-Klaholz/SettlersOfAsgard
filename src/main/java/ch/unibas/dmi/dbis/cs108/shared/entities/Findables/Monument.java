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
    public Monument(int id, String name, String description, int runes, boolean setBonus, List<Coordinates> tiles, String world) {
        super(id, name, description);
        this.runes = runes;
        this.setBonus = setBonus;
        this.tiles = tiles;
        this.world = world;
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
     * Gets if this Monument is part of a set
     *
     * @return The set of this Monument
     */
    public int getSetBonus() {
        return (runes * SETTINGS.Config.SET_BONUS_MULTIPLIER.getValue());
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
}