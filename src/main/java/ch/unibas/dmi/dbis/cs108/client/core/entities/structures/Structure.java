package ch.unibas.dmi.dbis.cs108.client.core.entities.structures;

public abstract class Structure {
    protected final int id;
    protected final String name;
    protected final String usage;
    protected final String description;
    protected final String useType;
    protected final int price;

    public Structure(StructureData data) {
        this.id = data.getId();
        this.name = data.getName();
        this.usage = data.getUsage();
        this.description = data.getDescription();
        this.useType = data.getUseType();
        this.price = data.getCost();
    }
}