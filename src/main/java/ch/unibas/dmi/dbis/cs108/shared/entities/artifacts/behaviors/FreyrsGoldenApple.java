package ch.unibas.dmi.dbis.cs108.shared.entities.artifacts.behaviors;

import ch.unibas.dmi.dbis.cs108.shared.entities.artifacts.Artifact;

/**
 * Class representing the behavior of Freyr's Golden Apple artifact.
 * This class implements the artifactBehavior interface and defines the specific behavior for Freyr's Golden Apple.
 */
public class FreyrsGoldenApple implements ArtifactBehavior {
    /**
     * Executes the behavior associated with Freyr's Golden Apple artifact.
     * @param artifact The artifact on which the behavior is executed.
     */
    @Override
    public void execute(Artifact artifact) {
        System.out.println("Freyr's Golden Apple is used.");
        System.out.println("Using Freyr's Golden Apple grants more energy.");
    }
}
