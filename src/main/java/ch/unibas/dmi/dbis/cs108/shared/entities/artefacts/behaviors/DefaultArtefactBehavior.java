package ch.unibas.dmi.dbis.cs108.shared.entities.artefacts.behaviors;

import ch.unibas.dmi.dbis.cs108.shared.entities.artefacts.Artefact;

/**
 * Default implementation of the ArtefactBehavior interface.
 * This class provides a default behavior for artefacts when no specific behavior is defined.
 */
public class DefaultArtefactBehavior implements ArtefactBehavior {
    /**
     * Executes the default behavior for the artefact.
     * This method can be overridden by subclasses to provide specific behavior.
     *
     * @param artefact The artefact on which the behavior is executed.
     */
    @Override
    public void execute(Artefact artefact) {
        // Default implementation
    }
}