package ch.unibas.dmi.dbis.cs108.client.ui.components;

import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    private Runnable onCloseAction;
    private Runnable onSaveAction;

    private final SimpleDoubleProperty volumeProperty = new SimpleDoubleProperty(50);
    private final BooleanProperty muteProperty = new SimpleBooleanProperty(false);
    private final StringProperty playerNameProperty = new SimpleStringProperty("Guest");
    private boolean isConnected = false;
    private String connectionStatusText = "Disconnected";

    /**
     * Creates a new SettingsDialog component.
     */
    public SettingsDialog() {
        super("");
        this.view = new StackPane();
        this.view.setId("settings-overlay");
        try {
            String cssPath = "/css/settings-dialog.css";
            var cssResource = getClass().getResource(cssPath);
            if (cssResource != null) {
                this.view.getStylesheets().add(cssResource.toExternalForm());
            } else {
                LOGGER.warning("Could not find CSS resource: " + cssPath);
                this.view.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
            }
        } catch (Exception e) {
            LOGGER.warning("Error loading CSS for SettingsDialog: " + e.getMessage());
            this.view.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        }
        this.view.setAlignment(Pos.CENTER);
        dialogContent = createDialogContent();
        StackPane.setAlignment(dialogContent, Pos.CENTER);
        this.view.getChildren().add(dialogContent);
        this.view.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.view.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        AnchorPane.setTopAnchor(this.view, 0.0);
        AnchorPane.setRightAnchor(this.view, 0.0);
        AnchorPane.setBottomAnchor(this.view, 0.0);
        AnchorPane.setLeftAnchor(this.view, 0.0);
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
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);
        content.setMaxHeight(500);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 10;");
        content.setOnMouseClicked(event -> event.consume());
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        content.setEffect(shadow);
        Text title = new Text(SETTINGS_TITLE);
        title.setFont(Font.font("Cinzel", FontWeight.BOLD, 24));
        title.setFill(Color.GOLD);
        VBox playerSection = createPlayerSection();
        VBox audioSection = createAudioSection();
        VBox connectionSection = createConnectionSection();
        HBox buttonsRow = createButtonsRow();
        content.getChildren().addAll(
                title,
                new Separator(),
                playerSection,
                new Separator(),
                audioSection,
                new Separator(),
                connectionSection,
                new Separator(),
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
        sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.WHITE);
        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Name:");
        nameLabel.setTextFill(Color.LIGHTGRAY);
        nameLabel.setMinWidth(80);
        TextField nameField = new TextField(playerNameProperty.get());
        nameField.textProperty().bindBidirectional(playerNameProperty);
        nameField.setMaxWidth(300);
        nameField.setPromptText("Enter your player name");
        nameField.setStyle(
                "-fx-background-color: #2c3347; -fx-text-fill: #e4c065; -fx-border-color: #e4c065; -fx-border-width: 1px; -fx-border-radius: 3px;");
        nameRow.getChildren().addAll(nameLabel, nameField);
        Label nameNoteLabel = new Label("Name changes will be applied when you save settings.");
        nameNoteLabel.setTextFill(Color.GRAY);
        nameNoteLabel.setStyle("-fx-font-style: italic;");
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
        sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.WHITE);
        HBox volumeRow = new HBox(10);
        volumeRow.setAlignment(Pos.CENTER_LEFT);
        Label volumeLabel = new Label("Volume:");
        volumeLabel.setTextFill(Color.LIGHTGRAY);
        volumeLabel.setMinWidth(80);
        Slider volumeSlider = new Slider(0, 100, volumeProperty.get());
        volumeSlider.valueProperty().bindBidirectional(volumeProperty);
        volumeSlider.setMinWidth(200);
        volumeSlider.setDisable(muteProperty.get());
        Label volumeValue = new Label(String.format("%.0f%%", volumeProperty.get()));
        volumeValue.setTextFill(Color.LIGHTGRAY);
        volumeValue.setMinWidth(50);
        volumeProperty.addListener((obs, oldVal, newVal) -> {
            volumeValue.setText(String.format("%.0f%%", newVal.doubleValue()));
        });
        volumeRow.getChildren().addAll(volumeLabel, volumeSlider, volumeValue);
        HBox muteRow = new HBox(10);
        muteRow.setAlignment(Pos.CENTER_LEFT);
        Label muteLabel = new Label("Mute Audio:");
        muteLabel.setTextFill(Color.LIGHTGRAY);
        muteLabel.setMinWidth(80);
        CheckBox muteCheckbox = new CheckBox();
        muteCheckbox.selectedProperty().bindBidirectional(muteProperty);
        muteCheckbox.setTextFill(Color.LIGHTGRAY);
        muteProperty.addListener((obs, oldVal, newVal) -> {
            volumeSlider.setDisable(newVal);
        });
        muteRow.getChildren().addAll(muteLabel, muteCheckbox);
        Label audioNoteLabel = new Label("Note: Audio is not yet implemented in this version.");
        audioNoteLabel.setTextFill(Color.GRAY);
        audioNoteLabel.setStyle("-fx-font-style: italic;");
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
        sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.WHITE);
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Status:");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setMinWidth(80);
        Label statusValue = new Label(connectionStatusText);
        statusValue.setTextFill(isConnected ? Color.LIGHTGREEN : Color.LIGHTCORAL);
        statusRow.getChildren().addAll(statusLabel, statusValue);
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
        saveButton.getStyleClass().add("settings-button");
        saveButton.getStyleClass().add("save-button");
        saveButton.setStyle(
                "-fx-background-color: #4a7a28; -fx-text-fill: #e8e8e8; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 5;");
        saveButton.setOnAction(e -> {
            if (onSaveAction != null) {
                onSaveAction.run();
            }
            close();
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("settings-button");
        cancelButton.setStyle(
                "-fx-background-color: #4a3f35; -fx-text-fill: #e8e8e8; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 5;");
        cancelButton.setOnAction(e -> close());
        buttonsRow.getChildren().addAll(cancelButton, saveButton);
        return buttonsRow;
    }

    /**
     * Shows the settings dialog with a fade-in animation.
     */
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
     */
    public void close() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), this.view);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            this.view.setVisible(false);
            this.view.setManaged(false);
            if (onCloseAction != null) {
                onCloseAction.run();
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
        if (this.view.isVisible()) {
            dialogContent.lookupAll(".label").stream()
                    .filter(node -> node instanceof Label && ((Label) node).getText().equals(this.connectionStatusText))
                    .map(node -> (Label) node)
                    .findFirst()
                    .ifPresent(label -> {
                        label.setText(statusText);
                        label.setTextFill(connected ? Color.LIGHTGREEN : Color.LIGHTCORAL);
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
     * Sets an action to be executed when the dialog is closed.
     *
     * @param action The action to execute on close.
     */
    public void setOnCloseAction(Runnable action) {
        this.onCloseAction = action;
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
     * Custom separator with styling.
     */
    private static class Separator extends Region {
        public Separator() {
            setPrefHeight(1);
            setMaxWidth(450);
            setStyle("-fx-background-color: #555555;");
            VBox.setMargin(this, new Insets(5, 0, 5, 0));
        }
    }
}
