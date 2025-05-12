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
     * String representing the curse of this statue
     */
    String curse;
    /**
     * String representing the deal this statue can make
     */
    String deal;
    /**
     * String representing the blessing of this statue
     */
    String blessing;
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
    public Statue() {
    }

    /**
     * Constructs a new Statue with specified values.
     *
     * @param id          The unique identifier for this statue
     * @param name        The name of this statue
     * @param description The description of this statue
     *   @param usage       The usage of this statue
     *    @param price       The price of this statue
     *     @param ressourceValue The resource value of this statue
     *    @param upgradePrice The cost to upgrade this statue
     *   @param world       The world in which this statue can be placed
     *    @param cardImagePath The path to the card image for this statue
     *     @param mapImagePath  The path to the map image for this statue
     *   @param curse       The curse of this statue
     *  @param deal        The deal this statue can make
     *   @param blessing    The blessing of this statue
     *
     */
    public Statue(int id, String name, int price, int ressourceValue, int upgradePrice, String description, String usage, String world, String cardImagePath, String mapImagePath, String curse, String deal, String blessing) {
        super(id, name, description, usage, price, ressourceValue, cardImagePath, mapImagePath);
        this.world = world;
        this.upgradePrice = upgradePrice;
        this.curse = curse;
        this.deal = deal;
        this.blessing = blessing;
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
     * Returns the cost to upgrade this statue.
     *
     * @return The upgrade price
     */
    public int getUpgradePrice() {
        return upgradePrice;
    }

    /**
     * Sets the cost to upgrade this statue.
     *
     * @param upgradePrice The upgrade price
     */
    public void setUpgradePrice(int upgradePrice) {
        this.upgradePrice = upgradePrice;
    }

    /**
     * Returns the current level of this statue.
     *
     * @return The statue's level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the level of the statue.
     *
     * @param level the level to set.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Returns the World in which the statue can be placed
     *
     * @return String the world
     */
    public String getWorld() {
        return world;
    }

    /**
     * Sets the world in which this statue can be placed.
     *
     * @param world The world
     */
    public void setWorld(String world) {
        this.world = world;
    }

    /**
     * Returns the curse of this statue.
     *
     * @return String the curse
     */
    public String getCurse() {
        return curse;
    }

    /**
     * Sets the curse of this statue.
     *
     * @param curse The curse
     */
    public void setCurse(String curse) {
        this.curse = curse;
    }

    /**
     * Returns the deal of this statue.
     *
     * @return String the deal
     */
    public String getDeal() {
        return deal;
    }

    /**
     * Sets the deal of this statue.
     *
     * @param deal The deal
     */
    public void setDeal(String deal) {
        this.deal = deal;
    }

    /**
     * Returns the blessing of this statue.
     *
     * @return String the blessing
     */
    public String getBlessing() {
        return blessing;
    }

    /**
     * Sets the blessing of this statue.
     *
     * @param blessing The blessing
     */
    public void setBlessing(String blessing) {
        this.blessing = blessing;
    }

    /**
     * Upgrades this statue to the next level.
     */
    public void upgrade() {
        level++;
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
        this.curse = json.get("curse").getAsString();
        this.deal = json.get("deal").getAsString();
        this.blessing = json.get("blessing").getAsString();
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
        clone.setCurse(this.curse);
        clone.setDeal(this.deal);
        clone.setBlessing(this.blessing);

        return (Statue) copyTo(clone);
    }

    /**
     * Defines the types of effects a statue can have based on its level.
     */
    public enum StatueEffectType {
        /**
         * Represents the type of effect a statue can have.
         * The level determines the type of effect:
         * - Level 1: no effect
         * - Level 2: deal effect
         * - Level 3: positive effect (high probability)
         * - Level 3: negative effect (low probability)
         */
        NONE,      // Level 1: no effect
        /**
         * Level 2: deal effect
         */
        DEAL,      // Level 2: deal effect
        /**
         * Level 3: positive effect (high probability)
         */
        BLESSING,  // Level 3: positive effect (high probability)
        /**
         * Level 3: negative effect (low probability)
         */
        CURSE      // Level 3: negative effect (low probability)
    }
}