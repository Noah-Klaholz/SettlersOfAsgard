package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.logging.Logger;

/**
 * Provides visual feedback for statue actions such as placement, upgrades,
 * or special abilities like Freyr's tree growing.
 */
public class StatueActionFeedback {
    /**
     * Logger for the StatueActionFeedback class.
     */
    private static final Logger LOGGER = Logger.getLogger(StatueActionFeedback.class.getName());

    /**
     * The pane that will contain the feedback elements.
     */
    private final Pane parentPane;

    /**
     * Creates a new statue action feedback handler.
     *
     * @param parentPane The pane that will contain the feedback elements
     */
    public StatueActionFeedback(Pane parentPane) {
        this.parentPane = parentPane;
    }

    /**
     * Shows a success notification for a statue action.
     *
     * @param message The success message to display
     */
    public void showSuccessNotification(String message) {
        showNotification(message, "success-notification");
    }

    /**
     * Shows an error notification for a statue action.
     *
     * @param message The error message to display
     */
    public void showErrorNotification(String message) {
        showNotification(message, "error-notification");
    }

    /**
     * Shows a notification with the specified style.
     *
     * @param message    The message to display
     * @param styleClass The CSS style class to apply
     */
    private void showNotification(String message, String styleClass) {
        Label notificationLabel = new Label(message);
        notificationLabel.getStyleClass().addAll("notification-label", styleClass);
        notificationLabel.setMaxWidth(300);
        notificationLabel.setWrapText(true);
        notificationLabel.setPadding(new Insets(10));

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.BLACK);
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        notificationLabel.setEffect(shadow);

        StackPane notificationPane = new StackPane(notificationLabel);
        notificationPane.setAlignment(Pos.BOTTOM_CENTER);
        notificationPane.setPadding(new Insets(0, 0, 50, 0));

        parentPane.getChildren().add(notificationPane);

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Stay visible
        Timeline stayVisible = new Timeline(
                new KeyFrame(Duration.millis(2000), new KeyValue(notificationPane.opacityProperty(), 1)));

        // Fade out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notificationPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> parentPane.getChildren().remove(notificationPane));

        // Play animations in sequence
        fadeIn.setOnFinished(e -> stayVisible.play());
        stayVisible.setOnFinished(e -> fadeOut.play());
        fadeIn.play();
    }

    /**
     * Shows a tree growing animation for Freyr's ability.
     *
     * @param x The x coordinate of the tile
     * @param y The y coordinate of the tile
     */
    public void showTreeGrowingAnimation(int x, int y) {
        // This would be implemented to show a custom animation for tree growing
        // at the specified coordinates

        LOGGER.info("Showing tree growing animation at (" + x + ", " + y + ")");

        // For now, just show a success notification
        showSuccessNotification("Tree successfully grown at (" + x + ", " + y + ")");
    }

    /**
     * Shows an upgrade animation for a statue.
     *
     * @param x        The x coordinate of the statue
     * @param y        The y coordinate of the statue
     * @param newLevel The new level of the statue
     */
    public void showUpgradeAnimation(int x, int y, int newLevel) {
        // This would be implemented to show a custom animation for statue upgrading

        LOGGER.info("Showing upgrade animation at (" + x + ", " + y + ") to level " + newLevel);

        // For now, just show a success notification
        showSuccessNotification("Statue upgraded to level " + newLevel);
    }
}
