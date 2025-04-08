package ch.unibas.dmi.dbis.cs108.shared.entities.entities.artefacts;
/**
 * Abstract class representing an artefact in the game.
 * Artefacts are items that can be found and used by players.
 */
public class Artefact {

    protected final String name;
    protected final String description;
    protected final String useType;
    protected final int artifactID;
    protected final double chance;
    protected final double effect;

    /**
     * Constructor for Artefact.
     *
     * @param data The data object containing artefact information.
     */
    public Artefact(ArtefactData data) {
        this.artifactID = data.getId();
        this.name = data.getName();
        this.description = data.getDescription();
        this.useType = data.getUseType();
        this.chance = data.getChance();
        this.effect = data.getEffect();
    }

    /**
     * Get the artifact ID.
     *
     * @return int artifactID
     */
    public int getArtifactID() {
        return artifactID;
    }

    /**
     * Get the name of the artefact.
     *
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the description of the artefact.
     *
     * @return String description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the use type of the artefact.
     *
     * @return String useType
     */
    public String getUseType() {
        return useType;
    }

    /**
     * Get the chance of finding the artefact.
     *
     * @return double chance
     */

    public double getChance() {
        return chance;
    }

    public void useArtifact(ArtefactData data) {

    }

    public double getEffect(){
        return effect;
    }



}