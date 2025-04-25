package ch.unibas.dmi.dbis.cs108.shared.entities.Findables;

import com.google.gson.JsonObject;

/**
 * Artifact entity that can be found and used by players.
 * Artifacts provide special one-time or reusable effects when used.
 * Artifacts can be field-targeted or player-targeted.
 */
public class Artifact extends FindableEntity {
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
        TRAP("Trap"),
        /**
         * A descriptor entity. Can be used to describe artifacts in general.
         */
        DESCRIPTOR("Descriptor");

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
     * The chance of finding this artifact.
     * This value is between 0 and 1, where 1 means 100% chance to find.
     */
    private double chanceToFind;

    /**
     * The effect of this artifact.
     * This value represents the magnitude of the artifact's effect.
     * In the case of Traps, it represents the id of the structure to place as an ActiveTrap.
     */
    private double effect;

    /**
     * Default constructor for Artifact.
     */
    public Artifact() {}

    /**
     * Constructs a new Artifact with specified values.
     *
     * @param id The unique identifier for this artifact
     * @param name The name of this artifact
     * @param description The description of this artifact
     * @param useType The type of functionality this artifact provides
     * @param chanceToFind The chance to find this artifact
     * @param effect The effect of this artifact
     */
    public Artifact(int id, String name, String description, String usage, String useType, double chanceToFind, double effect) {
        super(id, name, description, usage);
        this.chanceToFind = chanceToFind;
        this.effect = effect;
        this.useType = UseType.fromString(useType);
    }

    /**
     * Sets the type of this artifact.
     *
     * @param useType The type of this artifact
     */
    public void setUseType(UseType useType) {
        this.useType = useType;
    }

    /**
     * Sets the chance to find this artifact.
     *
     * @param chanceToFind The chance to find this artifact
     */
    public void setChanceToFind(double chanceToFind) {
        this.chanceToFind = chanceToFind;
    }

    /**
     * Sets the effect of this artifact.
     *
     * @param effect The effect of this artifact
     */
    public void setEffect(double effect) {
        this.effect = effect;
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
     * Gets the effect of this artifact.
     *
     * @return The effect of this artifact
     */
    public double getChanceToFind() {
        return chanceToFind;
    }

    /**
     * Gets the effect of this artifact.
     *
     * @return The effect of this artifact
     */
    public double getEffect() {
        return effect;
    }

    /**
     * Loads artifact data from a JSON object.
     * Extends the parent method to also load artifact-specific data.
     *
     * @param json The JSON object containing artifact data
     */
    @Override
    protected void loadFromJson(JsonObject json) {
        super.loadFromJson(json);
        if (json.has("useType")) {
            useType = UseType.fromString(json.get("useType").getAsString());
        } else {
            throw new IllegalArgumentException("Missing useType in JSON");
        }
        this.chanceToFind = json.get("chance").getAsDouble();
        this.effect = json.get("effect").getAsDouble();
    }

    /**
     * Factory method to create an artifact from JSON data.
     *
     * @param json The JSON object containing artifact data
     * @return A new Artifact instance populated with data from the JSON
     */
    public static Artifact fromJson(JsonObject json) {
        Artifact artifact = new Artifact();
        artifact.loadFromJson(json);
        return artifact;
    }

    /**
     * Returns a clone of this Artifact.
     * This method creates a new instance of the Artifact with the same properties as the original.
     *
     * @return A new Artifact object that is a clone of this one
     */
    @Override
    public Artifact clone() {
        Artifact clone = new Artifact();

        clone.setUseType(this.useType);
        clone.setChanceToFind(this.chanceToFind);
        clone.setEffect(this.effect);

        return (Artifact) copyTo(clone);
    }
}