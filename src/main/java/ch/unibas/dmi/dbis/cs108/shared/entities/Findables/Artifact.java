package ch.unibas.dmi.dbis.cs108.shared.entities.Findables;

import com.google.gson.JsonObject;

/**
 * Artifact entity that can be found and used by players.
 * Artifacts provide special one-time or reusable effects when used.
 * Artifacts can be field-targeted or player-targeted.
 */
public class Artifact extends FindableEntity {
    /**
     * The type of functionality this artifact provides when used.
     */
    public enum UseType {
        /**
         * A player-targeted artifact. Can be used on oneself or another player.
         */
        PLAYER("Player"),
        /**
         * A field-targeted artifact. Can be used on a single field.
         */
        FIELD("Field"),
        /**
         * A trap artifact. Can be used to set a trap on a non-owned empty field.
         */
        TRAP("Trap");

        private final String type;

        UseType(String type) {this.type = type;}

        public String getType() {return type;}

        public static UseType fromString(String type) {
            for (UseType u : UseType.values()) {
                if (u.type.equalsIgnoreCase(type)) {
                    return u;
                }
            }
            throw new IllegalArgumentException("Unknown use type: " + type);
        }
    }

    private UseType useType;

    /**
     * Whether this artifact can be used multiple times.
     */
    private boolean reusable;

    /**
     * The number of uses remaining (if reusable).
     */
    private int usesRemaining;

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
     * @param targetType The type of target this artifact affects
     * @param reusable Whether this artifact can be used multiple times
     * @param usesRemaining The number of uses remaining (if reusable)
     */
    public Artifact(int id, String name, String description, String useType,
                    String targetType, boolean reusable, int usesRemaining) {
        super(id, name, description, targetType);
        this.useType = UseType.fromString(useType);
        this.reusable = reusable;
        this.usesRemaining = usesRemaining;
    }

    /**
     * Returns the use type of this artifact.
     *
     * @return The use type identifier
     */
    public UseType getUseType() { return useType; }

    /**
     * Checks if this artifact is reusable.
     *
     * @return true if reusable, false otherwise
     */
    public boolean isReusable() { return reusable; }

    /**
     * Returns the number of uses remaining.
     *
     * @return Number of uses remaining
     */
    public int getUsesRemaining() { return usesRemaining; }

    /**
     * Records that the artifact has been used once.
     * Decrements the uses remaining counter.
     *
     * @return true if the artifact can still be used again, false if it's depleted
     */
    public boolean use() {
        if (usesRemaining > 0) {
            usesRemaining--;
            return usesRemaining > 0;
        }
        return false;
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
        this.useType = UseType.fromString(json.get("useType").getAsString());
        this.reusable = json.get("reusable").getAsBoolean();
        this.usesRemaining = json.get("usesRemaining").getAsInt();
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
}