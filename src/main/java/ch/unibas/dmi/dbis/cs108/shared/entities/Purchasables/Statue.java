package ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables;

import com.google.gson.JsonObject;

/**
 * Statue entity that can be purchased, placed, and upgraded.
 * Statues provide special functionality to players and can be upgraded
 * to increase their effectiveness.
 */
public class Statue extends PurchasableEntity {
    /**
     * The type of functionality this statue provides when used.
     */
    private String useType;

    /**
     * The cost to upgrade this statue to the next level.
     */
    private int upgradePrice;

    /**
     * The current level of this statue.
     */
    private int level = 1;

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
     * @param useType The type of functionality this statue provides
     */
    public Statue(int id, String name, String description, String useType) {
        super(id, name, description, 0); // Price from JSON
        this.useType = useType;
    }

    /**
     * Returns the use type of this statue.
     *
     * @return The use type identifier
     */
    public String getUseType() { return useType; }

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
     * Upgrades this statue to the next level.
     */
    public void upgrade() { level++; }

    /**
     * Loads statue data from a JSON object.
     * Extends the parent method to also load use type and upgrade price data.
     *
     * @param json The JSON object containing statue data
     */
    @Override
    protected void loadFromJson(JsonObject json) {
        super.loadFromJson(json);
        this.useType = json.get("useType").getAsString();
        this.upgradePrice = json.get("upgradePrice").getAsInt();
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
}