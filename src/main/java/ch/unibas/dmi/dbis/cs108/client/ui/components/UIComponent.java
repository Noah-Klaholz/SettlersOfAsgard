package ch.unibas.dmi.dbis.cs108.client.ui.components;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for reusable UI components.
 *
 * @param <T> The type of JavaFX Node encapsulated by the component.
 */
public abstract class UIComponent<T extends Node> {
    private static final Logger logger = Logger.getLogger(UIComponent.class.getName());
    protected T view;

    public UIComponent(String fxmlPath) {
        if (fxmlPath == null || fxmlPath.isEmpty()) {
            // No FXML to load, view will be created programmatically by subclass
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setController(this);
            this.view = loader.load();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load component from: " + fxmlPath, e);
            throw new RuntimeException("Failed to load component from: " + fxmlPath, e);
        }
    }

    public T getView() {
        return view;
    }
}