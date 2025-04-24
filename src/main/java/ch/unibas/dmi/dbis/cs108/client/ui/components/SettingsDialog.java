package ch.unibas.dmi.dbis.cs108.client.ui.components;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.StylesheetLoader;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.logging.Logger;

/**
 * A reusable Settings dialog component that allows users to adjust game
 * settings,
 * including player name, audio settings, and connection status.
 */
public class SettingsDialog extends UIComponent<StackPane> {
    private static final Logger LOGGER = Logger.getLogger(SettingsDialog.class.getName());
    private static final String SETTINGS_TITLE = "Game Settings";

    private final VBox dialogContent;
    private Runnable onSaveAction;

    private final SimpleDoubleProperty volumeProperty = new SimpleDoubleProperty(50);
    private final BooleanProperty muteProperty = new SimpleBooleanProperty(false);
    private final StringProperty playerNameProperty = new SimpleStringProperty("Guest");
    private boolean isConnected = false;
    private String connectionStatusText = "Disconnected";
    private Label statusValueLabel; // Keep reference to update style class

    /**
     * Creates a new SettingsDialog component.
     */
    public SettingsDialog() {
        super("");
        this.view = new StackPane();
        this.view.setId("settings-overlay");
        this.view.getStyleClass().add("dialog-overlay"); // Add style class

        // Load stylesheets using the StylesheetLoader utility
        StylesheetLoader.loadDialogStylesheets(this.view);
        StylesheetLoader.loadStylesheet(this.view, "/css/settings-dialog.css");

        this.view.setAlignment(Pos.CENTER);
        dialogContent = createDialogContent();
        StackPane.setAlignment(dialogContent, Pos.CENTER);
        this.view.getChildren().add(dialogContent);
        this.view.setViewOrder(-100);
        this.view.setOnMouseClicked(event -> {
            if (event.getTarget() == this.view) {
                close();
                event.consume();
            }
        });
        this.view.setVisible(false);
        this.view.setManaged(false);
    }

    /**
     * Creates the content of the dialog.
     *
     * @return A VBox containing the dialog content.
     */
    private VBox createDialogContent() {
        VBox content = new VBox(15);
        content.getStyleClass().add("dialog-content-box"); // Use style class
        content.setAlignment(Pos.CENTER);
        content.setOnMouseClicked(event -> event.consume());

        Text title = new Text(SETTINGS_TITLE);
        title.getStyleClass().add("dialog-title"); // Use style class

        VBox playerSection = createPlayerSection();
        VBox audioSection = createAudioSection();
        VBox connectionSection = createConnectionSection();
        HBox buttonsRow = createButtonsRow();

        content.getChildren().addAll(
                title,
                new DialogSeparator(), // Use custom styled separator
                playerSection,
                new DialogSeparator(),
                audioSection,
                new DialogSeparator(),
                connectionSection,
                new DialogSeparator(),
                buttonsRow);
        return content;
    }

    /**
     * Creates the player name settings section.
     *
     * @return VBox containing player settings controls.
     */
    private VBox createPlayerSection() {
        VBox playerSection = new VBox(10);
        playerSection.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("Player Settings");
        sectionTitle.getStyleClass().add("dialog-section-title"); // Use style class

        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Name:");
        nameLabel.getStyleClass().add("dialog-label"); // Use style class

        TextField nameField = new TextField(playerNameProperty.get());
        nameField.textProperty().bindBidirectional(playerNameProperty);
        nameField.setMaxWidth(300); // Keep max width for layout control
        nameField.setPromptText("Enter your player name");
        nameField.getStyleClass().add("dialog-textfield"); // Use style class

        nameRow.getChildren().addAll(nameLabel, nameField);

        Label nameNoteLabel = new Label("Name changes will be applied when you save settings.");
        nameNoteLabel.getStyleClass().add("dialog-note"); // Use style class

        playerSection.getChildren().addAll(sectionTitle, nameRow, nameNoteLabel);
        return playerSection;
    }

    /**
     * Creates the audio settings section.
     *
     * @return VBox containing audio settings controls.
     */
    private VBox createAudioSection() {
        VBox audioSection = new VBox(10);
        audioSection.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("Audio Settings");
        sectionTitle.getStyleClass().add("dialog-section-title");

        HBox volumeRow = new HBox(10);
        volumeRow.setAlignment(Pos.CENTER_LEFT);
        Label volumeLabel = new Label("Volume:");
        volumeLabel.getStyleClass().add("dialog-label");

        Slider volumeSlider = new Slider(0, 100, volumeProperty.get());
        volumeSlider.valueProperty().bindBidirectional(volumeProperty);
        volumeSlider.setMinWidth(200); // Keep min width
        volumeSlider.setDisable(muteProperty.get());

        Label volumeValue = new Label(String.format("%.0f%%", volumeProperty.get()));
        volumeValue.getStyleClass().add("dialog-label"); // Use dialog-label for consistency
        volumeValue.setMinWidth(50); // Keep min width
        volumeProperty.addListener((obs, oldVal, newVal) -> {
            volumeValue.setText(String.format("%.0f%%", newVal.doubleValue()));
        });
        volumeRow.getChildren().addAll(volumeLabel, volumeSlider, volumeValue);

        HBox muteRow = new HBox(10);
        muteRow.setAlignment(Pos.CENTER_LEFT);
        Label muteLabel = new Label("Mute Audio:");
        muteLabel.getStyleClass().add("dialog-label");

        CheckBox muteCheckbox = new CheckBox();
        muteCheckbox.selectedProperty().bindBidirectional(muteProperty);
        muteProperty.addListener((obs, oldVal, newVal) -> {
            volumeSlider.setDisable(newVal);
        });
        muteRow.getChildren().addAll(muteLabel, muteCheckbox);

        Label audioNoteLabel = new Label("Note: Audio is not yet implemented in this version.");
        audioNoteLabel.getStyleClass().add("dialog-note");

        audioSection.getChildren().addAll(sectionTitle, volumeRow, muteRow, audioNoteLabel);
        return audioSection;
    }

    /**
     * Creates the connection status section.
     *
     * @return VBox containing connection status information.
     */
    private VBox createConnectionSection() {
        VBox connectionSection = new VBox(10);
        connectionSection.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("Connection Status");
        sectionTitle.getStyleClass().add("dialog-section-title");

        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Status:");
        statusLabel.getStyleClass().add("dialog-label");

        statusValueLabel = new Label(connectionStatusText); // Assign to field
        statusValueLabel.getStyleClass().add("dialog-connection-status"); // Add base class
        statusValueLabel.getStyleClass().add(isConnected ? "connected" : "disconnected"); // Add specific class

        statusRow.getChildren().addAll(statusLabel, statusValueLabel);
        connectionSection.getChildren().addAll(sectionTitle, statusRow);
        return connectionSection;
    }

    /**
     * Creates the buttons row for the dialog.
     *
     * @return HBox containing dialog buttons.
     */
    private HBox createButtonsRow() {
        HBox buttonsRow = new HBox(10);
        buttonsRow.setAlignment(Pos.CENTER_RIGHT);

        Button saveButton = new Button("Save Settings");
        saveButton.getStyleClass().addAll("dialog-button", "dialog-button-save"); // Use style classes
        saveButton.setOnAction(e -> {
            if (onSaveAction != null) {
                onSaveAction.run();
            }
            close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("dialog-button", "dialog-button-cancel"); // Use style classes
        cancelButton.setOnAction(e -> close());

        buttonsRow.getChildren().addAll(cancelButton, saveButton);
        return buttonsRow;
    }

    /**
     * Shows the settings dialog with a fade-in animation.
     */
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
     * Closes the settings dialog with a fade-out animation.
     * Calls the onCloseAction if set.
     */
    public void close() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), this.view);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            this.view.setVisible(false);
            this.view.setManaged(false);
            // Remove the view from its parent after fade out
            if (this.view.getParent() instanceof Pane parentPane) {
                parentPane.getChildren().remove(this.view);
            }
            // Use the inherited getter
            Runnable action = getOnCloseAction();
            if (action != null) {
                action.run();
            }
        });
        fadeOut.play();
    }

    /**
     * Sets the connection status to be displayed.
     *
     * @param connected  Whether the client is connected to the server.
     * @param statusText Status text to display.
     */
    public void setConnectionStatus(boolean connected, String statusText) {
        this.isConnected = connected;
        this.connectionStatusText = statusText;
        // Update label text and style classes when status changes
        if (statusValueLabel != null) {
            Platform.runLater(() -> {
                statusValueLabel.setText(statusText);
                statusValueLabel.getStyleClass().removeAll("connected", "disconnected");
                statusValueLabel.getStyleClass().add(connected ? "connected" : "disconnected");
            });
        }
    }

    /**
     * Gets the current volume property.
     *
     * @return SimpleDoubleProperty for volume.
     */
    public SimpleDoubleProperty volumeProperty() {
        return volumeProperty;
    }

    /**
     * Gets the mute property.
     *
     * @return BooleanProperty for mute status.
     */
    public BooleanProperty muteProperty() {
        return muteProperty;
    }

    /**
     * Gets the player name property.
     *
     * @return StringProperty for player name.
     */
    public StringProperty playerNameProperty() {
        return playerNameProperty;
    }

    /**
     * Sets an action to be executed when the save button is clicked.
     *
     * @param action The action to execute on save.
     */
    public void setOnSaveAction(Runnable action) {
        this.onSaveAction = action;
    }

    /**
     * Custom separator using CSS styling.
     */
    private static class DialogSeparator extends Region {
        public DialogSeparator() {
            getStyleClass().add("dialog-separator"); // Use style class
        }
    }
}
