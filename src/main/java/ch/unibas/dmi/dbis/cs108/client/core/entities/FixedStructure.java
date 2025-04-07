package ch.unibas.dmi.dbis.cs108.client.core.entities;

/**
 * Class representing a fixed structure in the game.
 * This class contains information about the structure's ID, name, description, and usage.
 */
public class FixedStructure {
    /**
     * The ID of the structure.
     */
    private final int id;
    /**
     * The name of the structure.
     */
    private final String name;
    /**
     * The description of the structure.
     */
    private final String description;
    /**
     * The usage of the structure.
     */
    private final String usage;
    /**
     * The x-coordinate of the structure.
     */
    private int x;
    /**
     * The y-coordinate of the structure.
     */
    private int y;

    /**
     * Constructor for FixedStructure.
     * This constructor initializes the FixedStructure with the provided data.
     *
     * @param id          The ID of the structure.
     * @param name        The name of the structure.
     * @param description The description of the structure.
     * @param usage       The usage of the structure.
     */
    public FixedStructure(int id, String name, String description, String usage) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.usage = usage;
    }

    /**
     * Gets the name of the structure.
     *
     * @return The name of the structure.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the Identifier of the structure.
     *
     * @return The identifier of the structure.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the Description of the structure.
     *
     * @return The description of the structure.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the Usage of the structure.
     *
     * @return The Usage of the structure.
     */
    public String getUsage() {
        return usage;
    }
}
