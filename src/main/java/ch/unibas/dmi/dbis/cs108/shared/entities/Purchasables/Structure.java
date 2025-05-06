package ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables;

import com.google.gson.JsonObject;

/**
 * Structure entity that can be purchased and placed on tiles.
 * Structures provide special functionality to players when used.
 */
public class Structure extends PurchasableEntity {

    /**
     * Default constructor for Structure.
     */
    public Structure() {
    }

    /**
     * Constructs a new Structure with specified values.
     *
     * @param id          The unique identifier for this structure
     * @param name        The name of this structure
     * @param description The description of this structure
     * @param price       The purchase price of this structure
     */
    public Structure(int id, String name, String description, String usage, int price, int resourceValue, String cardImagePath, String mapImagePath) {
        super(id, name, description, usage, price, resourceValue, cardImagePath, mapImagePath);
    }

    /**
     * Factory method to create a structure from JSON data.
     *
     * @param json The JSON object containing structure data
     * @return A new Structure instance populated with data from the JSON
     */
    public static Structure fromJson(JsonObject json) {
        Structure structure = new Structure();
        structure.loadFromJson(json);
        return structure;
    }

    /**
     * Loads structure data from a JSON object.
     * Extends the parent method to also load use type data.
     *
     * @param json The JSON object containing structure data
     */
    @Override
    protected void loadFromJson(JsonObject json) {
        super.loadFromJson(json);
    }

    /**
     * Returns a clone of this Structure.
     * This method creates a new instance of the Structure with the same properties as the original.
     *
     * @return A new Structure object that is a clone of this one
     */
    @Override
    public Structure clone() {
        Structure clone = new Structure();
        return (Structure) copyTo(clone);
    }
}