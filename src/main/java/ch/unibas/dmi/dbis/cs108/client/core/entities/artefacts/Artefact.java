package ch.unibas.dmi.dbis.cs108.client.core.entities.artefacts;

public abstract class Artefact {
    protected final String name;
    protected final String description;
    protected final String useType;
    protected final int id;
    protected final double chance;

    public Artefact(ArtefactData data) {
        this.id = data.getId();
        this.name = data.getName();
        this.description = data.getDescription();
        this.useType = data.getUseType();
        this.chance = data.getChance();
    }
}
