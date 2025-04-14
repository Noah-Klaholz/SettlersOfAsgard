package ch.unibas.dmi.dbis.cs108.shared.entities.artefacts.behaviors;

import ch.unibas.dmi.dbis.cs108.shared.entities.artefacts.Artefact;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class that provides artefact behaviors based on the artefact name.
 * This class is responsible for registering and retrieving the appropriate behavior for each artefact.
 */
public class ArtefactBehaviorProvider {
    /**
     * Singleton instance of ArtefactBehaviorProvider.
     */
    private static final ArtefactBehaviorProvider INSTANCE = new ArtefactBehaviorProvider();
    /**
     * Map that associates artefact names with their corresponding behaviors.
     */
    private final Map<String, ArtefactBehavior> behaviors = new HashMap<>();
    /**
     * Default behavior to be used when no specific behavior is found for an artefact.
     */
    private final ArtefactBehavior defaultBehavior = new DefaultArtefactBehavior();

    /**
     * Private constructor to prevent instantiation from outside.
     * Initializes the behavior map with default behaviors.
     */
    private ArtefactBehaviorProvider() {
        registerBehaviors();
    }

    /**
     * Returns the singleton instance of ArtefactBehaviorProvider.
     *
     * @return ArtefactBehaviorProvider instance
     */
    public static ArtefactBehaviorProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Registers the default behaviors for artefacts.
     * This method should be called during the initialization of the application.
     */
    private void registerBehaviors() {
        behaviors.put("Freyja's Necklace", new FreyjasNecklaceBehavior());
        behaviors.put("Odin's Eye", new OdinsEyeBehavior());
        // Register other behaviors
    }

    /**
     * Registers a new behavior for a specific artefact name.
     *
     * @param artefactName The name of the artefact
     * @return ArtefactBehavior The behavior associated with the artefact name
     */
    public ArtefactBehavior getBehaviorFor(String artefactName) {
        return behaviors.getOrDefault(artefactName, defaultBehavior);
    }

    /**
     * Registers a new behavior for a specific artefact name.
     *
     * @param artefact The  of the artefact
     */
    public void executeArtefactBehavior(Artefact artefact) {
        getBehaviorFor(artefact.getName()).execute(artefact);
    }
}