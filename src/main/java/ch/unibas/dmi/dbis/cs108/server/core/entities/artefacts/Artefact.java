package ch.unibas.dmi.dbis.cs108.server.core.entities.artefacts;

/**
 * Abstract class representing an artefact in the game.
 * Artefacts are items that can be found and used by players.
 */
public abstract class Artefact {
    protected final String name; // The name of the artefact
    protected final String description; // The description of the artefact
    protected final String useType; // The type of use for the artefact
    protected final int id; // The ID of the artefact
    protected final double chance; // The chance of finding the artefact

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
