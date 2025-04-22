package ch.unibas.dmi.dbis.cs108.client.ui.components;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for reusable UI components, optionally loading their layout from
 * FXML.
 *
 * @param <T> The type of JavaFX Node encapsulated by the component.
 */
public abstract class UIComponent<T extends Node> {
    private static final Logger logger = Logger.getLogger(UIComponent.class.getName());
    /** The root JavaFX Node of this component. */
    protected T view;

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
}
