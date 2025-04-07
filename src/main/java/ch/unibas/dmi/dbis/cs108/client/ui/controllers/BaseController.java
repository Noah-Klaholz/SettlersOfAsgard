package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;

/**
 * Abstract base controller that holds common dependencies.
 * In a real application you might use a DI framework to inject these.
 */
public abstract class BaseController {
    protected final ResourceLoader resourceLoader;
    protected final UIEventBus eventBus;
    protected final SceneManager sceneManager;

    // In a DI framework these dependencies would be injected.
    public BaseController(ResourceLoader resourceLoader, UIEventBus eventBus, SceneManager sceneManager) {
        this.resourceLoader = resourceLoader;
        this.eventBus = eventBus;
        this.sceneManager = sceneManager;
    }

    // Common helper methods, logging, error handling, etc. can go here.
}