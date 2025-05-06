package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.components.UIComponent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.logging.Logger;

/**
 * Abstract base controller that holds common dependencies for UI controllers.
 */
public abstract class BaseController {
    private static final Logger LOGGER = Logger.getLogger(BaseController.class.getName());

    /**
     * Resource loader for loading resources.
     */
    protected final ResourceLoader resourceLoader;
    /**
     * UIEvent bus for handling events.
     */
    protected final UIEventBus eventBus;
    /**
     * Scene manager for managing scenes.
     */
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

    /**
     * Shows a UIComponent dialog as an overlay on top of the provided root pane.
     * Handles detaching the dialog from previous parents, adding it correctly based
     * on the root pane type (BorderPane or StackPane), and restoring the original
     * layout when the dialog is closed. Preserves any existing onCloseAction set on
     * the dialog.
     *
     * @param dialog   The UIComponent dialog to show.
     * @param rootPane The root pane (BorderPane or StackPane) where the overlay
     *                 should appear.
     * @param <T>      The specific type of Pane used by the UIComponent's view.
     */
    protected <T extends Pane> void showDialogAsOverlay(UIComponent<T> dialog, Pane rootPane) {
        if (dialog == null || rootPane == null) {
            LOGGER.warning("Cannot show dialog overlay: Dialog or rootPane is null.");
            return;
        }

        T dialogView = dialog.getView();
        Runnable originalOnCloseAction = dialog.getOnCloseAction();

        // Detach from previous parent if needed
        if (dialogView.getParent() instanceof Pane parentPane) {
            parentPane.getChildren().remove(dialogView);
        }

        if (rootPane instanceof BorderPane borderPane) {
            Node previousCenter = borderPane.getCenter();
            if (previousCenter instanceof StackPane tempContainer && tempContainer.getChildren().contains(dialogView)) {
                LOGGER.fine("Adjusting previous center: was dialog container.");
                previousCenter = tempContainer.getChildren().get(0);
            }
            final Node finalPreviousCenter = previousCenter;

            StackPane container = new StackPane(finalPreviousCenter, dialogView);
            StackPane.setAlignment(dialogView, Pos.CENTER);
            borderPane.setCenter(container);

            dialog.setOnCloseAction(() -> {
                if (borderPane.getCenter() == container) {
                    borderPane.setCenter(finalPreviousCenter);
                }
                if (originalOnCloseAction != null) {
                    originalOnCloseAction.run();
                }
            });
            LOGGER.fine("Showing dialog overlay in BorderPane center.");

        } else if (rootPane instanceof StackPane stackPane) {
            if (!stackPane.getChildren().contains(dialogView)) {
                stackPane.getChildren().add(dialogView);
                StackPane.setAlignment(dialogView, Pos.CENTER);
            }
            dialog.setOnCloseAction(() -> {
                if (originalOnCloseAction != null) {
                    originalOnCloseAction.run();
                }
            });
            LOGGER.fine("Showing dialog overlay in StackPane.");

        } else {
            LOGGER.warning("Unsupported root pane type for overlay: " + rootPane.getClass().getName());
            return;
        }

        dialog.show();
    }
}
