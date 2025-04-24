package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;

/**
 * Abstract base controller that holds common dependencies for UI controllers.
 */
public abstract class BaseController {
    /** Resource loader for loading resources. */
    protected final ResourceLoader resourceLoader;
    /** UIEvent bus for handling events. */
    protected final UIEventBus eventBus;
    /** Scene manager for managing scenes. */
    protected final SceneManager sceneManager;

    /**
     * Constructs a BaseController with the required dependencies.
     *
     * @param resourceLoader The resource loader for loading resources.
     * @param eventBus       The event bus for handling events.
     * @param sceneManager   The scene manager for managing scenes.
     */
    public BaseController(ResourceLoader resourceLoader, UIEventBus eventBus, SceneManager sceneManager) {
        this.resourceLoader = resourceLoader;
        this.eventBus = eventBus;
        this.sceneManager = sceneManager;
    }
}
