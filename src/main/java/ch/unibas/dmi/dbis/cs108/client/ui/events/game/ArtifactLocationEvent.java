package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class ArtifactLocationEvent implements UIEvent {
    /**
     * The ID of the artifact.
     */
    private final int artifactId;
    /**
     * The X coordinate of the artifact's location on the tile map.
     */
    private final int tileX; // Corresponds to column
    /**
     * The Y coordinate of the artifact's location on the tile map.
     */
    private final int tileY; // Corresponds to row
    /** Boolean representing if an artifact was found*/
    private final boolean isArtifactFound;

    /**
     * Constructs an ArtifactLocationEvent with the specified artifact ID and
     * coordinates.
     *
     * @param artifactId the ID of the artifact
     * @param tileX      the X coordinate of the artifact's location
     * @param tileY      the Y coordinate of the artifact's location
     * @param isArtifactFound represents whether an artifact has been found
     */
    public ArtifactLocationEvent(int artifactId, int tileX, int tileY, boolean isArtifactFound) {
        this.artifactId = artifactId;
        this.tileX = tileX;
        this.tileY = tileY;
        this.isArtifactFound = isArtifactFound
    }

    /**
     * Returns the ID of the artifact.
     *
     * @return the artifact ID
     */
    public int getArtifactId() {
        return artifactId;
    }

    /**
     * Returns the X coordinate of the artifact's location.
     *
     * @return the X coordinate
     */
    public int getTileX() {
        return tileX;
    }

    /**
     * Returns the Y coordinate of the artifact's location.
     *
     * @return the Y coordinate
     */
    public int getTileY() {
        return tileY;
    }

    /**
     * @return true if an artifact was found, false otherwise
     */
    public boolean isArtifactFound() {
        return isArtifactFound;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "ARTIFACT_LOCATION_EVENT";
    }

    /**
     * Returns a string representation of the ArtifactLocationEvent.
     *
     * @return a string representation of the event
     */
    @Override
    public String toString() {
        return "ArtifactLocationEvent{" +
                "artifactId=" + artifactId +
                ", tileX=" + tileX +
                ", tileY=" + tileY +
                '}';
    }
}
