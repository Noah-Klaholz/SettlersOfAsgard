package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.fxml.FXML;

import java.util.logging.Logger;

/**
 * Controller for the settings screen.
 * Manages UI elements related to game settings (e.g., volume, graphics).
 */
public class SettingsController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(SettingsController.class.getName());

    // Add FXML elements for settings controls here
    // @FXML private Slider volumeSlider;
    // @FXML private CheckBox fullscreenCheckbox;

    /**
     * Constructor requiring dependencies.
     * These are typically injected or provided via a factory.
     */
    public SettingsController() {
        // In a real application, dependencies would likely be injected.
        // For now, we manually instantiate them, assuming they are singletons
        // or provided appropriately elsewhere.
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
        LOGGER.info("SettingsController created.");
    }

    @FXML
    private void initialize() {
        LOGGER.info("Initializing settings screen.");
        // Load current settings and populate controls
        // Add listeners to controls to handle changes
        // Example:
        // volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> handleVolumeChange(newVal.doubleValue()));
    }

    @FXML
    private void handleSaveChanges() {
        LOGGER.info("Save settings button clicked.");
        // Gather values from controls
        // Persist settings (e.g., to a config file or preferences)
        // Optionally provide feedback to the user
        // Navigate back or close the settings view
        // sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU); // Example navigation
    }

    @FXML
    private void handleCancel() {
        LOGGER.info("Cancel button clicked.");
        // Discard any changes made
        // Navigate back or close the settings view
        // sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU); // Example navigation
    }

    // Add methods to handle specific setting changes, e.g.:
    // private void handleVolumeChange(double newVolume) { ... }
    // private void loadSettings() { ... }
}
