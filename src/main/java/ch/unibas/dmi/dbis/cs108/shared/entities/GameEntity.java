package ch.unibas.dmi.dbis.cs108.shared.entities;

import com.google.gson.JsonObject;

/**
 * Base abstract class for all game entities.
 * Provides common properties and functionality for all game entities
 * such as identification, naming, and description.
 */
public abstract class GameEntity {
    /**
     * Unique identifier for the entity.
     */
    protected int id;

    /**
     * Name of the entity.
     */
    protected String name;

    /**
     * Description of the entity.
     */
    protected String description;

    /**
     * Default constructor for GameEntity.
     */
    public GameEntity() {}

    /**
     * Constructs a new GameEntity with specified values.
     *
     * @param id The unique identifier for this entity
     * @param name The name of this entity
     * @param description The description of this entity
     */
    public GameEntity(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the unique identifier of this entity.
     *
     * @return The entity's ID
     */
    public int getId() { return id; }

    /**
     * Returns the name of this entity.
     *
     * @return The entity's name
     */
    public String getName() { return name; }

    /**
     * Returns the description of this entity.
     *
     * @return The entity's description
     */
    public String getDescription() { return description; }

    /**
     * Loads entity data from a JSON object.
     * Populates the entity's fields with values from the provided JSON.
     *
     * @param json The JSON object containing entity data
     */
    protected void loadFromJson(JsonObject json) {
        this.id = json.get("id").getAsInt();
        this.name = json.get("name").getAsString();
        this.description = json.get("description").getAsString();
    }
}