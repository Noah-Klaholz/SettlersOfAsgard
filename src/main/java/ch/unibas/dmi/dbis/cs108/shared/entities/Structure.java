package ch.unibas.dmi.dbis.cs108.shared.entities;

public class Structure {

    private final int structureID;
    private final String name;
    private final String description;
    private final String useType;
    private final int price;
    private final int resourceValue;

    public Structure(int structureID, String name, String description, String useType, int price) {
        this.structureID = structureID;
        this.name = name;
        this.description = description;
        this.useType = useType;
        this.price = price;
        //todo: later: have resourceValue as parameter and calculate: chunks of the map having more and some chunks having less
        this.resourceValue = 60;
    }

    public int getResourceValue() {
        return resourceValue;
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
