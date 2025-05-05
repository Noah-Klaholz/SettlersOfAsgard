package ch.unibas.dmi.dbis.cs108.client.ui.components.game.popups;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.geometry.Insets;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A reusable contextual menu popup for game entities.
 */
public class ContextualMenu extends Popup {

    private final VBox container;

    public ContextualMenu() {
        container = new VBox(5);
        container.setPadding(new Insets(10));
        // Apply existing popup/menu styles
        container.getStyleClass().add("contextual-menu");
        setAutoHide(true);
        getContent().add(container);
    }

    /**
     * Sets the actions available in the menu.
     *
     * @param actions         A map where keys are action names (e.g., "Upgrade",
     *                        "Use Ability")
     *                        and values are Runnables to execute when the action is
     *                        chosen.
     * @param disabledActions A list of action names that should be disabled.
     */
    public void setActions(Map<String, Runnable> actions, List<String> disabledActions) {
        container.getChildren().clear();
        if (actions == null || actions.isEmpty()) {
            // Optionally add a "No actions available" label
            return;
        }

        actions.forEach((actionName, action) -> {
            Button actionButton = new Button(actionName);
            actionButton.getStyleClass().add("menu-button"); // Reuse existing menu button style
            actionButton.setOnAction(e -> {
                action.run();
                hide();
            });
            if (disabledActions != null && disabledActions.contains(actionName)) {
                actionButton.setDisable(true);
                // Apply existing disabled style if not handled by CSS :disabled pseudo-class
                actionButton.getStyleClass().add("disabled-button");
            }
            container.getChildren().add(actionButton);
        });
    }
}
