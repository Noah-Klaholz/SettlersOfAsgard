package ch.unibas.dmi.dbis.cs108.shared.entities.entities.artefacts.behaviors;

import ch.unibas.dmi.dbis.cs108.shared.entities.entities.artefacts.Artefact;

import java.util.HashMap;
import java.util.Map;

public class ArtefactBehaviorProvider {
    private static final ArtefactBehaviorProvider INSTANCE = new ArtefactBehaviorProvider();
    private final Map<String, ArtefactBehavior> behaviors = new HashMap<>();
    private final ArtefactBehavior defaultBehavior = new DefaultArtefactBehavior();

    private ArtefactBehaviorProvider() {
        registerBehaviors();
    }

    public static ArtefactBehaviorProvider getInstance() {
        return INSTANCE;
    }

    private void registerBehaviors() {
        behaviors.put("Freyja's Necklace", new FreyjasNecklaceBehavior());
        behaviors.put("Odin's Eye", new OdinsEyeBehavior());
        // Register other behaviors
    }

    public ArtefactBehavior getBehaviorFor(String artefactName) {
        return behaviors.getOrDefault(artefactName, defaultBehavior);
    }

    public void executeArtefactBehavior(Artefact artefact) {
        getBehaviorFor(artefact.getName()).execute(artefact);
    }
}