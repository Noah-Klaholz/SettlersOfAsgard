package ch.unibas.dmi.dbis.cs108.server.core.entities;

/**
 * Class representing an artefact in the game.
 * Artefacts are items that can be found and used by players.
 */
public class Artefact {
    private final int artifactID;
    private final String name;
    private final String description;
    private final String useType;

    /**
     * Constructor for Artefact.
     *
     * @param artifactID  The ID of the artefact.
     * @param name        The name of the artefact.
     * @param description A description of the artefact.
     * @param useType     The type of use for the artefact.
     */
    public Artefact(int artifactID, String name, String description, String useType) {
        this.artifactID = artifactID;
        this.name = name;
        this.description = description;
        this.useType = useType;
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


}
