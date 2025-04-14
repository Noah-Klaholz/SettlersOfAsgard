package ch.unibas.dmi.dbis.cs108.shared.entities.Findables;

import com.google.gson.JsonObject;

/**
 * Trap entity that can be found and used by players.
 * Traps provide negative effects to targets when activated.
 * Traps can be field-targeted or player-targeted.
 */
public class Trap extends FindableEntity {
    /**
     * The type of effect this trap causes when activated.
     */
    private String effectType;

    /**
     * The strength or magnitude of the trap effect.
     */
    private int effectStrength;

    /**
     * The number of rounds this trap remains active.
     */
    private int duration;

    /**
     * Default constructor for Trap.
     */
    public Trap() {}

    /**
     * Constructs a new Trap with specified values.
     *
     * @param id The unique identifier for this trap
     * @param name The name of this trap
     * @param description The description of this trap
     * @param effectType The type of effect this trap causes
     * @param targetType The type of target this trap affects
     * @param effectStrength The strength of the trap effect
     * @param duration The number of rounds this trap remains active
     */
    public Trap(int id, String name, String description, String effectType,
                String targetType, int effectStrength, int duration) {
        super(id, name, description, targetType);
        this.effectType = effectType;
        this.effectStrength = effectStrength;
        this.duration = duration;
    }

    /**
     * Returns the effect type of this trap.
     *
     * @return The effect type identifier
     */
    public String getEffectType() { return effectType; }

    /**
     * Returns the strength of the trap effect.
     *
     * @return The effect strength
     */
    public int getEffectStrength() { return effectStrength; }

    /**
     * Returns the duration of the trap effect.
     *
     * @return The number of rounds the trap remains active
     */
    public int getDuration() { return duration; }

    /**
     * Loads trap data from a JSON object.
     * Extends the parent method to also load trap-specific data.
     *
     * @param json The JSON object containing trap data
     */
    @Override
    protected void loadFromJson(JsonObject json) {
        super.loadFromJson(json);
        this.effectType = json.get("effectType").getAsString();
        this.effectStrength = json.get("effectStrength").getAsInt();
        this.duration = json.get("duration").getAsInt();
    }

    /**
     * Factory method to create a trap from JSON data.
     *
     * @param json The JSON object containing trap data
     * @return A new Trap instance populated with data from the JSON
     */
    public static Trap fromJson(JsonObject json) {
        Trap trap = new Trap();
        trap.loadFromJson(json);
        return trap;
    }
}