package ch.unibas.dmi.dbis.cs108.shared.entities;

import ch.unibas.dmi.dbis.cs108.shared.entities.Behaviors.Parameter;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Monument;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;
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
     * Usage of the entity.
     */
    protected String usage;

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
    public GameEntity(int id, String name, String description, String usage) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.usage = usage;
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
     * Returns the usage of this entity.
     *
     * @return The entity's usage
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Returns the list of parameters associated with this entity.
     *
     * @return The list of parameters
     */
    public List<Parameter> getParams() { return params; }


    /**
     * Returns if this entity is a structure.
     *
     * @return true if it is, false otherwise
     */
    public boolean isStructure() { return this instanceof Structure; }

    /**
     * Returns if this entity is a statue.
     *
     * @return true if it is, false otherwise
     */
    public boolean isStatue() { return this instanceof Statue; }

    /**
     * Returns if this entity is a Monument
     *
     * @return true if it is, false otherwise
     */
    public boolean isMonument() { return this instanceof Monument; }

    /**
     * Sets the value of the parameter at the specified index to the given value
     *
     * @param index the index of the parameter to have its value replaced
     * @param value double that represents the value for the param
     */
    public void setParam(int index, double value) {
        Parameter param = new Parameter(params.get(index).getName(), value);
        params.set(index, param);
    }
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
     *    "params": [10, 20] // list of parameters
     * }
     *
     * @param json The JSON object containing entity data.
     */
    protected void loadFromJson(JsonObject json) {
        this.id = json.get("id").getAsInt();
        this.name = json.get("name").getAsString();
        this.description = json.get("description").getAsString();
        this.usage = json.get("usage").getAsString();

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

    /**
     * Creates a deep copy of this entity
     *
     * @return A new instance with identical properties
     */
    public abstract GameEntity clone();

    /**
     * Copies basic entity properties from this entity to the clone
     *
     * @param clone The entity to copy properties to
     * @return The clone with copied properties
     */
    protected GameEntity copyTo(GameEntity clone) {
        // Copy basic properties
        clone.id = this.id;
        clone.name = this.name;
        clone.description = this.description;
        clone.usage = this.usage;

        // Deep copy parameters
        clone.params = new ArrayList<>();
        for (Parameter param : this.params) {
            clone.params.add(new Parameter(param.getName(), param.getValue()));
        }

        return clone;
    }

    /**
     * Returns the price of this entity.
     * If the entity is a Structure or Statue, it retrieves the price from that class.
     * If not, it returns 0.
     *
     * @return The price of the entity
     */
    public int getPrice() {
        if (this instanceof PurchasableEntity pe) {
            return pe.getPrice();
        }
        return 0;
    }
}