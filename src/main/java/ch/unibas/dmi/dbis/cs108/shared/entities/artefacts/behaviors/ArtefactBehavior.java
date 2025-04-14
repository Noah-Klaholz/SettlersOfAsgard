package ch.unibas.dmi.dbis.cs108.shared.entities.artefacts.behaviors;

import ch.unibas.dmi.dbis.cs108.shared.entities.artefacts.Artefact;

/**
 * Interface for defining behaviors of artefacts.
 * Implementations of this interface will define specific actions that can be performed on artefacts.
 */
public interface ArtefactBehavior {
    /**
     * Executes the behavior associated with the artefact.
     *
     * @param artefact The artefact on which the behavior is executed.
     */
    void execute(Artefact artefact);
}