package ch.unibas.dmi.dbis.cs108.client.ui.events.game;

import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEvent;

/**
 * Represents the response/result of a UsePlayerArtifact action.
 */
public class UsePlayerArtifactResponseEvent implements UIEvent {

    private final boolean success;
    private final String message;
    private final String artifactId;
    private final String targetPlayer;

    /**
     * Constructs a UsePlayerArtifactResponseEvent.
     *
     * @param success      whether the action was successful
     * @param message      optional message for success/failure
     * @param artifactId   the ID of the artifact
     * @param targetPlayer the target player, if applicable
     */
    public UsePlayerArtifactResponseEvent(boolean success, String message, String artifactId, String targetPlayer) {
        this.success = success;
        this.message = message;
        this.artifactId = artifactId;
        this.targetPlayer = targetPlayer;
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
     * @return the ID of the artifact
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @return the target player, if applicable
     */
    public String getTargetPlayer() {
        return targetPlayer;
    }

    /**
     * @return the event type identifier
     */
    @Override
    public String getType() {
        return "USE_PLAYER_ARTIFACT_RESPONSE";
    }
}
