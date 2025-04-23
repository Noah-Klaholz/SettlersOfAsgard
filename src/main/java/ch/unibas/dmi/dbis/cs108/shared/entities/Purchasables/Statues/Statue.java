package ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues;

import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import com.google.gson.JsonObject;

/**
 * Statue entity that can be purchased, placed, and upgraded.
 * Statues provide special functionality to players and can be upgraded
 * to increase their effectiveness.
 */
public class Statue extends PurchasableEntity {
    /**
     * The cost to upgrade this statue to the next level.
     */
    private int upgradePrice;

    /**
     * The current level of this statue.
     * Level 1 is the default level.
     */
    private int level = 1;

    /**
     * String representing the world in which this statue can be placed
     */
    private String world;

    /**
     * Default constructor for Statue.
     */
    public Statue() {}

    /**
     * Constructs a new Statue with specified values.
     *
     * @param id The unique identifier for this statue
     * @param name The name of this statue
     * @param description The description of this statue
     */
    public Statue(int id, String name, int price, int ressourceValue, int upgradePrice, String description, String world) {
        super(id, name, description, price, ressourceValue);
        this.world = world;
        this.upgradePrice = upgradePrice;
    }

    /**
     * Sets the cost to upgrade this statue.
     *
     * @param upgradePrice The upgrade price
     */
    public void setUpgradePrice(int upgradePrice) { this.upgradePrice = upgradePrice; }

    /**
     * Sets the world in which this statue can be placed.
     *
     * @param world The world
     */
    public void setWorld(String world) { this.world = world; }

    /**
     * Returns the cost to upgrade this statue.
     *
     * @return The upgrade price
     */
    public int getUpgradePrice() { return upgradePrice; }

    /**
     * Returns the current level of this statue.
     *
     * @return The statue's level
     */
    public int getLevel() { return level; }

    /**
     * Returns the World in which the statue can be placed
     *
     * @return String the world
     */
    public String getWorld() { return world; }

    /**
     * Upgrades this statue to the next level.
     */
    public void upgrade() { level++; }

    /**
     * Sets the level of the statue.
     *
     * @param level the level to set.
     */
    public void setLevel(int level) { this.level = level; }

    /**
     * Defines the types of effects a statue can have based on its level.
     */
    public enum StatueEffectType {
        NONE,      // Level 1: no effect
        DEAL,      // Level 2: deal effect
        BLESSING,  // Level 3: positive effect (high probability)
        CURSE      // Level 3: negative effect (low probability)
    }

    /**
     * Loads statue data from a JSON object.
     * Extends the parent method to also load use type and upgrade price data.
     *
     * @param json The JSON object containing statue data
     */
    @Override
    protected void loadFromJson(JsonObject json) {
        super.loadFromJson(json);
        this.upgradePrice = json.get("upgradePrice").getAsInt();
        this.world = json.get("world").getAsString();
    }

    /**
     * Factory method to create a statue from JSON data.
     *
     * @param json The JSON object containing statue data
     * @return A new Statue instance populated with data from the JSON
     */
    public static Statue fromJson(JsonObject json) {
        Statue statue = new Statue();
        statue.loadFromJson(json);
        return statue;
    }

    /**
     * Returns a clone of this Statue.
     * This method creates a new instance of the Statue with the same properties as the original.
     *
     * @return A new Statue object that is a clone of this one
     */
    @Override
    public Statue clone() {
        Statue clone = new Statue();
        clone.setUpgradePrice(this.upgradePrice);
        clone.setWorld(this.world);

        return (Statue) copyTo(clone);
    }
}