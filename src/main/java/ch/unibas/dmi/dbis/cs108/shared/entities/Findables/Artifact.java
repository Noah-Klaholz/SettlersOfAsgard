package ch.unibas.dmi.dbis.cs108.shared.entities.Findables;

import com.google.gson.JsonObject;

/**
 * Artifact entity that can be found and used by players.
 * Artifacts provide special one-time or reusable effects when used.
 * Artifacts can be field-targeted or player-targeted.
 */
public class Artifact extends FindableEntity {
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
    public Artifact(int id, String name, String description, String useType) {
        super(id, name, description, useType);
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