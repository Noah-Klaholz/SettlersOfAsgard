package ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables;

import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import com.google.gson.JsonObject;

/**
 * Base class for entities that can be purchased by players.
 * Extends the GameEntity class with price information.
 */
public abstract class PurchasableEntity extends GameEntity {
    /**
     * The cost to purchase this entity.
     */
    protected int price;

    /**
     * Default constructor for PurchasableEntity.
     */
    public PurchasableEntity() {}

    /**
     * Constructs a new PurchasableEntity with specified values.
     *
     * @param id The unique identifier for this entity
     * @param name The name of this entity
     * @param description The description of this entity
     * @param price The purchase price of this entity
     */
    public PurchasableEntity(int id, String name, String description, int price) {
        super(id, name, description);
        this.price = price;
    }

    /**
     * Returns the purchase price of this entity.
     *
     * @return The price of this entity
     */
    public int getPrice() { return price; }

    /**
     * Loads entity data from a JSON object.
     * Extends the parent method to also load price data.
     *
     * @param json The JSON object containing entity data
     */
    @Override
    protected void loadFromJson(JsonObject json) {
        super.loadFromJson(json);
        this.price = json.get("price").getAsInt();
    }
}