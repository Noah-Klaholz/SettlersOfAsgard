package ch.unibas.dmi.dbis.cs108.shared.entities.Findables;

import com.google.gson.JsonObject;

/**
 * Artifact entity that can be found and used by players.
 * Artifacts provide special one-time or reusable effects when used.
 * Artifacts can be field-targeted or player-targeted.
 */
public class Artifact extends FindableEntity {
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
     */
    public Artifact(int id, String name, String description, String useType, double chanceToFind, double effect) {
        super(id, name, description, useType);
        this.chanceToFind = chanceToFind;
        this.effect = effect;
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
}