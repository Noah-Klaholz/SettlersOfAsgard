package ch.unibas.dmi.dbis.cs108.shared.entities.artifacts.behaviors;

import ch.unibas.dmi.dbis.cs108.shared.entities.artifacts.Artifact;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class that provides artifact behaviors based on the artifact name.
 * This class is responsible for registering and retrieving the appropriate behavior for each artifact.
 */
public class ArtifactBehaviorProvider {
    /**
     * Singleton instance of artifactBehaviorProvider.
     */
    private static final ArtifactBehaviorProvider INSTANCE = new ArtifactBehaviorProvider();
    /**
     * Map that associates artifact names with their corresponding behaviors.
     */
    private final Map<String, ArtifactBehavior> behaviors = new HashMap<>();
    /**
     * Default behavior to be used when no specific behavior is found for an artifact.
     */
    private final ArtifactBehavior defaultBehavior = new DefaultArtifactBehavior();

    /**
     * Private constructor to prevent instantiation from outside.
     * Initializes the behavior map with default behaviors.
     */
    private ArtifactBehaviorProvider() {
        registerBehaviors();
    }

    /**
     * Returns the singleton instance of artifactBehaviorProvider.
     *
     * @return artifactBehaviorProvider instance
     */
    public static ArtifactBehaviorProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Registers the default behaviors for artifacts.
     * This method should be called during the initialization of the application.
     */
    private void registerBehaviors() {
        behaviors.put("Freyja's Necklace", new FreyjasNecklaceBehavior());
        behaviors.put("Odin's Eye", new OdinsEyeBehavior());
        // Register other behaviors
    }

    /**
     * Registers a new behavior for a specific artifact name.
     *
     * @param artifactName The name of the artifact
     * @return artifactBehavior The behavior associated with the artifact name
     */
    public ArtifactBehavior getBehaviorFor(String artifactName) {
        return behaviors.getOrDefault(artifactName, defaultBehavior);
    }

    /**
     * Registers a new behavior for a specific artifact name.
     *
     * @param artifact The  of the artifact
     */
    public void executeartifactBehavior(Artifact artifact) {
        getBehaviorFor(artifact.getName()).execute(artifact);
    }
}