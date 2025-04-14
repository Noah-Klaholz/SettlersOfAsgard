package ch.unibas.dmi.dbis.cs108.shared.entities.structures;

public class StructureData {
    private int id;
    private String name;
    private String usage;
    private String description;
    private String useType;
    private int cost;
    private int ressourceValue;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    public String getUseType() {
        return useType;
    }

    public int getCost() {
        return cost;
    }

    public int getRessourceValue() {
        return ressourceValue;
    }
}