package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.client.ui.components.UIComponent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.StylesheetLoader;
import javafx.animation.FadeTransition;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * A reusable confirmation dialog for statue placement, upgrades, and actions.
 * Displays title, description, image, cost, and confirm/cancel buttons.
 */
public class ConfirmationDialog extends UIComponent<StackPane> {
    private static final Logger LOGGER = Logger.getLogger(ConfirmationDialog.class.getName());

    private final VBox dialogContent;
    private Consumer<Boolean> resultHandler;
    private Runnable onConfirmAction;
    private Runnable onCancelAction;

    private final Label costLabel;
    private final Label descriptionLabel;
    private final ImageView imageView;
    private final Label titleLabel;

    /**
     * Creates a new confirmation dialog.
     */
    public ConfirmationDialog() {
        super("");
        this.view = new StackPane();
        this.view.getStyleClass().add("dialog-overlay");

        StylesheetLoader.loadDialogStylesheets(this.view);

        this.view.setAlignment(Pos.CENTER);

        // Initialize components
        titleLabel = new Label("Confirm Action");
        titleLabel.getStyleClass().add("dialog-title");

        imageView = new ImageView();
        imageView.setFitHeight(120);
        imageView.setFitWidth(120);
        imageView.setPreserveRatio(true);

        descriptionLabel = new Label();
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("dialog-description");

        costLabel = new Label();
        costLabel.getStyleClass().add("dialog-cost");

        // Create buttons
        Button confirmButton = new Button("Confirm");
        confirmButton.getStyleClass().addAll("dialog-button", "confirm-button");
        confirmButton.setOnAction(e -> {
            if (onConfirmAction != null)
                onConfirmAction.run();
            if (resultHandler != null)
                resultHandler.accept(true);
            close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("dialog-button", "cancel-button");
        cancelButton.setOnAction(e -> {
            if (onCancelAction != null)
                onCancelAction.run();
            if (resultHandler != null)
                resultHandler.accept(false);
            close();
        });

        HBox buttonBox = new HBox(10, cancelButton, confirmButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Create dialog content
        dialogContent = new VBox(15);
        dialogContent.getStyleClass().add("dialog-content-box");
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setMaxWidth(400);
        dialogContent.setMaxHeight(500);
        dialogContent.setPadding(new javafx.geometry.Insets(20));
        dialogContent.setOnMouseClicked(Event::consume);

        // Add components to dialog
        dialogContent.getChildren().addAll(
                titleLabel,
                new DialogSeparator(),
                imageView,
                descriptionLabel,
                costLabel,
                new DialogSeparator(),
                buttonBox);

        // Hide image by default until one is set
        imageView.setManaged(false);
        imageView.setVisible(false);

        StackPane.setAlignment(dialogContent, Pos.CENTER);
        this.view.getChildren().add(dialogContent);

        // Close on clicking outside the dialog
        this.view.setOnMouseClicked(event -> {
            if (event.getTarget() == this.view) {
                if (onCancelAction != null)
                    onCancelAction.run();
                if (resultHandler != null)
                    resultHandler.accept(false);
                close();
                event.consume();
            }
        });

        this.view.setVisible(false);
        this.view.setManaged(false);
    }

    /**
     * Sets the title of the dialog.
     * 
     * @param title The title to display
     * @return This dialog for method chaining
     */
    public ConfirmationDialog setTitle(String title) {
        titleLabel.setText(title);
        return this;
    }

    /**
     * Sets the description text of the dialog.
     * 
     * @param description The description to display
     * @return This dialog for method chaining
     */
    public ConfirmationDialog setDescription(String description) {
        descriptionLabel.setText(description);
        return this;
    }

    /**
     * Sets the cost text of the dialog.
     * 
     * @param cost The cost to display
     * @return This dialog for method chaining
     */
    public ConfirmationDialog setCost(String cost) {
        if (cost == null || cost.isEmpty()) {
            costLabel.setVisible(false);
            costLabel.setManaged(false);
        } else {
            costLabel.setText(cost);
            costLabel.setVisible(true);
            costLabel.setManaged(true);
        }
        return this;
    }

    /**
     * Sets the image to display in the dialog.
     * 
     * @param image The image to display
     * @return This dialog for method chaining
     */
    public ConfirmationDialog setImage(Image image) {
        if (image == null) {
            imageView.setManaged(false);
            imageView.setVisible(false);
        } else {
            imageView.setImage(image);
            imageView.setManaged(true);
            imageView.setVisible(true);
        }
        return this;
    }

    /**
     * Sets a handler to be called when the dialog is closed with a result.
     * 
     * @param resultHandler A consumer that receives true for confirm, false for
     *                      cancel
     * @return This dialog for method chaining
     */
    public ConfirmationDialog setResultHandler(Consumer<Boolean> resultHandler) {
        this.resultHandler = resultHandler;
        return this;
    }

    /**
     * Sets an action to be performed when the confirm button is clicked.
     * 
     * @param onConfirmAction The action to perform
     * @return This dialog for method chaining
     */
    public ConfirmationDialog setOnConfirmAction(Runnable onConfirmAction) {
        this.onConfirmAction = onConfirmAction;
        return this;
    }

    /**
     * Sets an action to be performed when the cancel button is clicked.
     * 
     * @param onCancelAction The action to perform
     * @return This dialog for method chaining
     */
    public ConfirmationDialog setOnCancelAction(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
        return this;
    }

    @Override
    public void show() {
        this.view.setVisible(true);
        this.view.setManaged(true);
        this.view.setOpacity(0);
        this.view.toFront();
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), this.view);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    /**
     * Closes the dialog with a fade-out animation.
     */
    public void close() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), this.view);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            this.view.setVisible(false);
            this.view.setManaged(false);
            // Remove from parent pane if needed
            if (this.view.getParent() instanceof Pane parentPane) {
                parentPane.getChildren().remove(this.view);
            }
            // Call the onClose action if set
            Runnable action = getOnCloseAction();
            if (action != null) {
                action.run();
            }
        });
        fadeOut.play();
    }

    /**
     * Custom separator using CSS styling.
     */
    private static class DialogSeparator extends Region {
        public DialogSeparator() {
            getStyleClass().add("dialog-separator");
        }
    }
}
