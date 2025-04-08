package ch.unibas.dmi.dbis.cs108.shared.entities.entities;

/**
 * Class representing a fixed structure in the game.
 * Fixed structures are non-movable entities that can be interacted with.
 */
public class FixedStructure {
    /**
     * The ID of the fixed structure.
     */
    private final int id;
    /**
     * The name of the fixed structure.
     */
    private final String name;
    /**
     * The description of the fixed structure.
     */
    private final String description;
    /**
     * The usage type of the fixed structure (e.g., "building", "furniture").
     */
    private final String usage;
    /**
     * The x-coordinate of the fixed structure.
     */
    private int x;
    /**
     * The y-coordinate of the fixed structure.
     */
    private int y;

    /**
     * Constructor for FixedStructure.
     *
     * @param id          The ID of the fixed structure.
     * @param name        The name of the fixed structure.
     * @param description The description of the fixed structure.
     * @param usage       The usage type of the fixed structure.
     */
    public FixedStructure(int id, String name, String description, String usage) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.usage = usage;
    }

    /**
     * Get the name of the fixed structure.
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the id of the fixed structure.
     * @return int id
     */
    public int getId() {
        return id;
    }

    /**
     * Get the description of the fixed structure.
     * @return String description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the usage of the fixed structure.
     * @return String usage
     */
    public String getUsage() {
        return usage;
    }
}
