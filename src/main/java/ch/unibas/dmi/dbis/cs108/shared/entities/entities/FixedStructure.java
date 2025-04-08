package ch.unibas.dmi.dbis.cs108.shared.entities.entities;

public class FixedStructure {
    private final int id;
    private final String name;
    private final String description;
    private final String usage;
    private int x;
    private int y;

    public FixedStructure(int id, String name, String description, String usage) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.usage = usage;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }
}
