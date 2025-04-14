package ch.unibas.dmi.dbis.cs108.shared.entities.artifacts.behaviors;

import ch.unibas.dmi.dbis.cs108.shared.entities.artifacts.Artifact;

/**
 * Default implementation of the artifactBehavior interface.
 * This class provides a default behavior for artifacts when no specific behavior is defined.
 */
public class DefaultArtifactBehavior implements ArtifactBehavior {
    /**
     * Executes the default behavior for the artifact.
     * This method can be overridden by subclasses to provide specific behavior.
     *
     * @param artifact The artifact on which the behavior is executed.
     */
    @Override
    public void execute(Artifact artifact) {
        // Default implementation
    }
}