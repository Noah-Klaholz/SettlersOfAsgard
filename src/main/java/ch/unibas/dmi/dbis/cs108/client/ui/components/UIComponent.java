package ch.unibas.dmi.dbis.cs108.client.ui.components;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.StylesheetLoader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UIComponent<T extends Node> {
    private static final Logger logger = Logger.getLogger(UIComponent.class.getName());
    /** The root JavaFX Node of this component. */
    protected T view;

    // Add onCloseAction support for dialog overlays
    private Runnable onCloseAction;

    /**
     * Constructs a UIComponent, optionally loading its view from the specified FXML
     * file.
     * If the fxmlPath is null or empty, the view should be created programmatically
     * by the subclass.
     *
     * @param fxmlPath The path to the FXML file relative to the component's class,
     *                 or null/empty if constructed programmatically.
     */
    public UIComponent(String fxmlPath) {
        if (fxmlPath == null || fxmlPath.isEmpty()) {
            logger.fine("No FXML path provided, component will be created programmatically");
            return;
        }
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("FXML resource not found: " + fxmlPath);
            }
            logger.fine("Loading UI component from: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setController(this);
            this.view = loader.load();
            logger.fine("Successfully loaded component from: " + fxmlPath);

            // Apply core stylesheets if view has been loaded successfully
            if (view instanceof Parent) {
                StylesheetLoader.loadCoreStylesheets((Parent) view);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load component from: " + fxmlPath, e);
            throw new RuntimeException("Failed to load component from: " + fxmlPath, e);
        }
    }

    /**
     * Returns the view node controlled by this component.
     *
     * @return The JavaFX node that represents this component's view.
     */
    public T getView() {
        return view;
    }

    /**
     * Makes the component visible. Subclasses must implement how visibility is
     * handled (e.g., setting properties, adding to parent, animations).
     * This method is called by BaseController.showDialogAsOverlay.
     */
    public abstract void show();

    /**
     * Sets an action to be executed when the dialog/component is closed.
     * Used by BaseController to restore overlays/layouts.
     * Subclasses should call this action when closing.
     */
    public void setOnCloseAction(Runnable action) {
        this.onCloseAction = action;
    }

    /**
     * Gets the current onCloseAction (may be null).
     */
    public Runnable getOnCloseAction() {
        return onCloseAction;
    }
}
