package ch.unibas.dmi.dbis.cs108.shared.entities.structures;

/**
 * Abstract class representing a structure in the game.
 */
public abstract class Structure {
    /**
     * The id of the structure.
     */
    protected final int id;
    /**
     * The name of the structure.
     */
    protected final String name;
    /**
     * The usage of the structure.
     */
    protected final String usage;
    /**
     * The description of the structure.
     */
    protected final String description;
    /**
     * The usage type of the structure (e.g., "building", "furniture").
     */
    protected final String useType;
    /**
     * The price of the structure.
     */
    protected final int price;

    /**
     * Constructor for Structure.
     *
     * @param data The data object containing the structure's information.
     */
    public Structure(StructureData data) {
        this.id = data.getId();
        this.name = data.getName();
        this.usage = data.getUsage();
        this.description = data.getDescription();
        this.useType = data.getUseType();
        this.price = data.getCost();
    }

    /**
     * Get the id of the structure.
     * @return int id
     */
    public int getId() {
        return id;
    }

    /**
     * Get the name of the structure.
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the usage of the structure.
     * @return String usage
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Get the description of the structure.
     * @return String description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the usage type of the structure.
     * @return String useType
     */
    public String getUseType() {
        return useType;
    }

    /**
     * Get the price of the structure.
     * @return int price
     */
    public int getPrice() {
        return price;
    }

    /**
     * Get the value of the structure.
     * @return int price
     */
    public int getRessourceValue() {
        return price; //TODO implement this correctly
    }
}