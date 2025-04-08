package ch.unibas.dmi.dbis.cs108.shared.entities.entities;

public class Structure {

    private final int structureID;
    private final String name;
    private final String description;
    private final String useType;
    private final int price;

    public Structure(int structureID, String name, String description, String useType, int price) {
        this.structureID = structureID;
        this.name = name;
        this.description = description;
        this.useType = useType;
        this.price = price;
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

    public int getPrice() {
        return price;
    }
}
