package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * UIEvent representing a request to use a field artifact in the game.
 */
public class UseFieldArtifactUIEvent implements UIEvent {
    private final int x;
    private final int y;
    private final int artifactId;
    private final String useType;

    /**
     * Constructs a new UseFieldArtifactUIEvent.
     *
     * @param x          the x-coordinate of the location where the artifact is to
     *                   be used
     * @param y          the y-coordinate of the location where the artifact is to
     *                   be used
     * @param artifactId the ID of the artifact being used
     * @param useType    the type of use (e.g., "use", "drop")
     */
    public UseFieldArtifactUIEvent(int x, int y, int artifactId, String useType) {
        this.x = x;
        this.y = y;
        this.artifactId = artifactId;
        this.useType = useType;
    }

    /**
     * Returns the x-coordinate of the location where the artifact is to be used.
     *
     * @return The x-coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the location where the artifact is to be used.
     *
     * @return The y-coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the ID of the artifact being used.
     *
     * @return The artifact ID.
     */
    public int getArtifactId() {
        return artifactId;
    }

    /**
     * Returns the type of use (e.g., "use", "drop").
     *
     * @return The use type.
     */
    public String getUseType() {
        return useType;
    }

    /**
     * Returns the type of this event.
     *
     * @return The type of this event.
     */
    @Override
    public String getType() {
        return "USE_FIELD_ARTIFACT";
    }
}
