package ch.unibas.dmi.dbis.cs108.shared.entities.Findables;

import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import com.google.gson.JsonObject;

/**
 * Base class for entities that can be found but not purchased.
 * These include artifacts and traps.
 */
public abstract class FindableEntity extends GameEntity {
    /**
     * The type of functionality this Entity provides when used.
     */
    public enum UseType {
        /**
         * A player-targeted entity. Can be used on oneself or another player.
         */
        PLAYER("Player"),
        /**
         * A field-targeted entity. Can be used on a single field.
         */
        FIELD("Field"),
        /**
         * A trap entity. Can be used to set a trap on a non-owned empty field.
         */
        TRAP("Trap");

        /**
         *  The type of this entity.
         */
        private final String type;

        /**
         * Constructor for UseType.
         *
         * @param type The type of this entity
         */
        UseType(String type) {this.type = type;}

        /**
         * Returns the type of this entity.
         *
         * @return The type of this entity
         */
        public String getType() {return type;}

        /**
         * Returns the UseType corresponding to the given string.
         *
         * @param type The string representation of the use type
         * @return The corresponding UseType
         * @throws IllegalArgumentException if the type is unknown
         */
        public static UseType fromString(String type) {
            for (UseType u : UseType.values()) {
                if (u.type.equalsIgnoreCase(type)) {
                    return u;
                }
            }
            throw new IllegalArgumentException("Unknown use type: " + type);
        }
    }

    /**
     * The type of target this entity affects.
     */
    private UseType useType;

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
     * @param useType The type of target this entity affects
     */
    public FindableEntity(int id, String name, String description, String useType) {
        super(id, name, description);
        this.useType = UseType.fromString(useType);
    }

    /**
     * Returns the target type of this findable entity.
     *
     * @return The target type (FIELD or PLAYER)
     */
    public UseType getUseType() {
        return useType;
    }

    /**
     * Checks if this entity targets a field.
     *
     * @return true if this entity targets a field, false otherwise
     */
    public boolean isFieldTarget() {
        return useType == UseType.FIELD || useType == UseType.TRAP;
    }

    /**
     * Checks if this entity targets a player.
     *
     * @return true if this entity targets a player, false otherwise
     */
    public boolean isPlayerTarget() {
        return useType == UseType.PLAYER;
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
        if (json.has("useType")) {
            useType = UseType.fromString(json.get("useType").getAsString());
        } else {
            throw new IllegalArgumentException("Missing useType in JSON");
        }
    }
}