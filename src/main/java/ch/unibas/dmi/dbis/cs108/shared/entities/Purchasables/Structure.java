package ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables;

import com.google.gson.JsonObject;

/**
 * Structure entity that can be purchased and placed on tiles.
 * Structures provide special functionality to players when used.
 */
public class Structure extends PurchasableEntity {
    /**
     * The type of functionality this structure provides when used.
     */
    private String useType;

    /**
     * Default constructor for Structure.
     */
    public Structure() {}

    /**
     * Constructs a new Structure with specified values.
     *
     * @param id The unique identifier for this structure
     * @param name The name of this structure
     * @param description The description of this structure
     * @param useType The type of functionality this structure provides
     * @param price The purchase price of this structure
     */
    public Structure(int id, String name, String description, String useType, int price) {
        super(id, name, description, price);
        this.useType = useType;
    }

    /**
     * Returns the use type of this structure.
     *
     * @return The use type identifier
     */
    public String getUseType() { return useType; }

    /**
     * Loads structure data from a JSON object.
     * Extends the parent method to also load use type data.
     *
     * @param json The JSON object containing structure data
     */
    @Override
    protected void loadFromJson(JsonObject json) {
        super.loadFromJson(json);
        this.useType = json.get("useType").getAsString();
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
}