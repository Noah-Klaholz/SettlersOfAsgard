package ch.unibas.dmi.dbis.cs108.shared.entities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

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
     * List for storing other parameters (e.g. how strong an effect is).
     */
    protected List<Parameter> params = new ArrayList<>();

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
     * Returns the list of parameters associated with this entity.
     *
     * @return The list of parameters
     */
    public List<Parameter> getParams() { return params; }

    /**
     * Loads entity data from a JSON object.
     * In addition to id, name and description, it loads a list of parameters
     * if available.
     *
     * Expected JSON format:
     * {
     *    "id": 1,
     *    "name": "Example Entity",
     *    "description": "Description text",
     *    "params": [10, 20]  // list of parameters
     * }
     *
     * @param json The JSON object containing entity data.
     */
    protected void loadFromJson(JsonObject json) {
        this.id = json.get("id").getAsInt();
        this.name = json.get("name").getAsString();
        this.description = json.get("description").getAsString();

        if (json.has("params")) {
            JsonArray jsonParams = json.getAsJsonArray("params");
            for (JsonElement element : jsonParams) {
                // Check if the element is an object containing a "value" key
                if (element.isJsonObject() && element.getAsJsonObject().has("value")) {
                    JsonObject paramObj = element.getAsJsonObject();
                    String paramName = paramObj.get("name").getAsString();
                    double paramValue = paramObj.get("value").getAsDouble();
                    params.add(new Parameter(paramName, paramValue));
                }
            }
        }
    }
}