package ch.unibas.dmi.dbis.cs108.shared.entities.artifacts.behaviors;

import ch.unibas.dmi.dbis.cs108.shared.entities.artifacts.Artifact;

/**
 * Interface for defining behaviors of artifacts.
 * Implementations of this interface will define specific actions that can be performed on artifacts.
 */
public interface ArtifactBehavior {
    /**
     * Executes the behavior associated with the artifact.
     *
     * @param artifact The artifact on which the behavior is executed.
     */
    void execute(Artifact artifact);
}