package ch.unibas.dmi.dbis.cs108.shared.entities.entities.artefacts;

public class Artefact {
    protected final int id;
    protected final String name;
    protected final String description;
    protected final String useType;
    protected final double chance;

    public Artefact(ArtefactData data) {
        this.id = data.getId();
        this.name = data.getName();
        this.description = data.getDescription();
        this.useType = data.getUseType();
        this.chance = data.getChance();
    }

    // Getters
    public int getId() {
        return id;
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

    public double getChance() {
        return chance;
    }
}