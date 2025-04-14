package ch.unibas.dmi.dbis.cs108.shared.entities.Findables;

import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import com.google.gson.JsonObject;

/**
 * Base class for entities that can be found but not purchased.
 * These include artifacts and traps.
 */
public abstract class FindableEntity extends GameEntity {
    /**
     * The type of target this entity affects (FIELD or PLAYER).
     */
    protected String targetType;

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
     * @param targetType The type of target this entity affects
     */
    public FindableEntity(int id, String name, String description, String targetType) {
        super(id, name, description);
        this.targetType = targetType;
    }

    /**
     * Returns the target type of this findable entity.
     *
     * @return The target type (FIELD or PLAYER)
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * Checks if this entity targets a field.
     *
     * @return true if this entity targets a field, false otherwise
     */
    public boolean isFieldTarget() {
        return "FIELD".equals(targetType);
    }

    /**
     * Checks if this entity targets a player.
     *
     * @return true if this entity targets a player, false otherwise
     */
    public boolean isPlayerTarget() {
        return "PLAYER".equals(targetType);
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
        this.targetType = json.get("targetType").getAsString();
    }
}