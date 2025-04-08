package ch.unibas.dmi.dbis.cs108.shared.entities.entities.artefacts;

/**
 * Class representing artefact data.
 * This class is used to store the data of an artefact.
 */
public class ArtefactData {
    private int id;
    private String name;
    private String description;
    private String useType;
    private double chance;

    /**
     * Getter method for the id of the artefact.
     * @return int id
     */
    public int getId() {
        return id;
    }

    /**
     * Setter method for the name of the artefact.
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter method for the description of the artefact.
     * @return String description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter method for the chance of the artefact.
     * @return double chance
     */
    public double getChance() {
        return chance;
    }

    /**
     * Getter method for the use type of the artefact.
     * @return String useType
     */
    public String getUseType() {
        return useType;
    }
}