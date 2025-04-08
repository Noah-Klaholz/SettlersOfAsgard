package ch.unibas.dmi.dbis.cs108.shared.entities.entities.artefacts.behaviors;

import ch.unibas.dmi.dbis.cs108.shared.entities.entities.artefacts.Artefact;

public class FreyrsGoldenApple implements ArtefactBehavior{
    @Override
    public void execute(Artefact artefact) {
        System.out.println("Freyr's Golden Apple is used.");
        System.out.println("Using Freyr's Golden Apple grants more energy.");
    }
}
