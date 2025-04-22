package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Represents the response/result of a UseFieldArtifact action.
 */
public class UseFieldArtifactResponseEvent implements UIEvent {

    private final boolean success;
    private final String message;
    private final int x;
    private final int y;
    private final String artifactId;

    /**
     * Constructs a UseFieldArtifactResponseEvent.
     *
     * @param success    whether the action was successful
     * @param message    optional message for success/failure
     * @param x          x-coordinate of the artifact
     * @param y          y-coordinate of the artifact
     * @param artifactId the ID of the artifact
     */
    public UseFieldArtifactResponseEvent(boolean success, String message, int x, int y, String artifactId) {
        this.success = success;
        this.message = message;
        this.x = x;
        this.y = y;
        this.artifactId = artifactId;
    }

    /**
     * @return true if the action was successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return the message associated with the response
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the x-coordinate of the artifact
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y-coordinate of the artifact
     */
    public int getY() {
        return y;
    }

    /**
     * @return the ID of the artifact
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "USE_FIELD_ARTIFACT_RESPONSE";
    }
}
