package ch.unibas.dmi.dbis.cs108.client.core.entities.artefacts;

public class ArtefactData {
    private int id;
    private String name;
    private String description;
    private String useType;
    private double chance;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getChance() {
        return chance;
    }

    public String getUseType() {
        return useType;
    }
}