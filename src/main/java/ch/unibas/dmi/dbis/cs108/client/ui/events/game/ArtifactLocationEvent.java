package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

public class ArtifactLocationEvent implements UIEvent {
    private final int artifactId;
    private final int tileX; // Corresponds to column
    private final int tileY; // Corresponds to row
    /** Boolean representing if an artifact was found*/
    private final boolean isArtifactFound;

    public ArtifactLocationEvent(int artifactId, int tileX, int tileY, boolean isArtifactFound) {
        this.artifactId = artifactId;
        this.tileX = tileX;
        this.tileY = tileY;
        this.isArtifactFound = isArtifactFound;
    }

    public int getArtifactId() {
        return artifactId;
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    /**
     * @return true if an artifact was found, false otherwise
     */
    public boolean isArtifactFound() {
        return isArtifactFound;
    }

    @Override
    public String getType() {
        return "ARTIFACT_LOCATION_EVENT";
    }

    @Override
    public String toString() {
        return "ArtifactLocationEvent{" +
                "artifactId=" + artifactId +
                ", tileX=" + tileX +
                ", tileY=" + tileY +
                '}';
    }
}
