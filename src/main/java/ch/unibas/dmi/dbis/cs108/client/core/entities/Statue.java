package ch.unibas.dmi.dbis.cs108.client.core.entities;

public class Statue {

    private final int statueID;
    private final String name;
    private final String description;
    private final String useType;

    public Statue(int statueID, String name, String description, String useType) {
        this.statueID = statueID;
        this.name = name;
        this.description = description;
        this.useType = useType;
    }

    public int getStatueID() {
        return statueID;
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
