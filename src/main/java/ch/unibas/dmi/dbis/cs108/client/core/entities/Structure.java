package ch.unibas.dmi.dbis.cs108.client.core.entities;

public class Structure {

    private final int structureID;
    private final String name;
    private final String description;
    private final String useType;
    private int price;

    public Structure(int structureID, String name, String description, String useType) {
        this.structureID = structureID;
        this.name = name;
        this.description = description;
        this.useType = useType;
    }

    public int getStructureID() {
        return structureID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUseType() {
        return useType;
    }
}
