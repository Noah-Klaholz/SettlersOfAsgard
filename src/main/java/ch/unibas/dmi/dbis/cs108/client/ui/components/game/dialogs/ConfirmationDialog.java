package ch.unibas.dmi.dbis.cs108.client.ui.components.game.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A reusable confirmation dialog.
 */
public class ConfirmationDialog extends Stage {

    private boolean confirmed = false;

    /**
     * Creates a confirmation dialog.
     *
     * @param ownerStage The parent stage.
     * @param title      The title of the dialog.
     * @param message    The confirmation message.
     */
    public ConfirmationDialog(Stage ownerStage, String title, String message) {
        initOwner(ownerStage);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED); // Use existing styling context

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        // Apply existing dialog styles if available, e.g., via a CSS class
        root.getStyleClass().add("confirmation-dialog"); // Assuming such a class exists or can be added

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dialog-title"); // Reuse existing title style

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("dialog-message"); // Reuse existing message style

        Button confirmButton = new Button("Confirm");
        confirmButton.getStyleClass().addAll("standard-button", "primary-button"); // Reuse existing button styles
        confirmButton.setOnAction(e -> {
            confirmed = true;
            close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("standard-button"); // Reuse existing button styles
        cancelButton.setOnAction(e -> {
            confirmed = false;
            close();
        });

        HBox buttonBox = new HBox(10, confirmButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(titleLabel, messageLabel, buttonBox);

        Scene scene = new Scene(root);
        // Link existing CSS if needed, assuming a central CSS management
        // scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        setScene(scene);
        setTitle(title); // Set title for window manager if style is not UNDECORATED
    }

    /**
     * Shows the dialog and waits for user input.
     *
     * @return true if the user confirmed, false otherwise.
     */
    public boolean showAndWaitConfirmation() {
        showAndWait();
        return confirmed;
    }
}
