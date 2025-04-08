package ch.unibas.dmi.dbis.cs108.shared.entities.entities.artefacts;

/**
 * Abstract class representing an artefact in the game.
 * Artefacts are items that can be found and used by players.
 */
public abstract class Artefact {
    /**
     * The name of the artefact
     */
    protected final String name;

    /**
     * The description of the artefact
     */
    protected final String description;

    /**
     * The type of use for the artefact
     */
    protected final String useType;

    /**
     * The ID of the artefact
     */
    protected final int id;

    /**
     * The chance of finding the artefact
     */
    protected final double chance;

    /**
     * Constructor for Artefact.
     *
     * @param data The data object containing artefact information.
     */
    public Artefact(ArtefactData data) {
        this.id = data.getId();
        this.name = data.getName();
        this.description = data.getDescription();
        this.useType = data.getUseType();
        this.chance = data.getChance();
    }
}