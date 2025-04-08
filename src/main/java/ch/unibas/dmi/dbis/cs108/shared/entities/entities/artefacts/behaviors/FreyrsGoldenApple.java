package ch.unibas.dmi.dbis.cs108.shared.entities.entities.artefacts.behaviors;

import ch.unibas.dmi.dbis.cs108.shared.entities.entities.artefacts.Artefact;

/**
 * Class representing the behavior of Freyr's Golden Apple artefact.
 * This class implements the ArtefactBehavior interface and defines the specific behavior for Freyr's Golden Apple.
 */
public class FreyrsGoldenApple implements ArtefactBehavior{
    /**
     * Executes the behavior associated with Freyr's Golden Apple artefact.
     * @param artefact The artefact on which the behavior is executed.
     */
    @Override
    public void execute(Artefact artefact) {
        System.out.println("Freyr's Golden Apple is used.");
        System.out.println("Using Freyr's Golden Apple grants more energy.");
    }
}
