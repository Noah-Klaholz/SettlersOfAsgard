package ch.unibas.dmi.dbis.cs108.shared.entities.Findables;

import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import com.google.gson.JsonObject;

/**
 * Base class for entities that can be found but not purchased.
 * These include artifacts and traps.
 */
public abstract class FindableEntity extends GameEntity {



    /**
     * Default constructor for FindableEntity.
     */
    public FindableEntity() {}

    /**
     * Constructs a new FindableEntity with specified values.
     *
     * @param id The unique identifier for this entity
     * @param name The name of this entity
     * @param description The description of this entity
     */
    public FindableEntity(int id, String name, String description, String usage) {
        super(id, name, description, usage);
    }

    /**
     * Loads entity data from a JSON object.
     * Extends the parent method to also load target type data.
     *
     * @param json The JSON object containing entity data
     */
    @Override
    protected void loadFromJson(JsonObject json) {
        super.loadFromJson(json);
    }
}