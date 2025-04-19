package ch.unibas.dmi.dbis.cs108.client.ui.components;

import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
 * A reusable Settings dialog component that allows users to adjust game settings.
 * This component creates a modal dialog with audio settings and connection status.
 */
public class SettingsDialog extends UIComponent<StackPane> {
    private static final Logger LOGGER = Logger.getLogger(SettingsDialog.class.getName());
    private static final String SETTINGS_TITLE = "Game Settings";
    
    private final VBox dialogContent;
    private Runnable onCloseAction;
    private Runnable onSaveAction;
    
    // Settings properties
    private final SimpleDoubleProperty volumeProperty = new SimpleDoubleProperty(50);
    private final BooleanProperty muteProperty = new SimpleBooleanProperty(false);
    private boolean isConnected = false;
    private String connectionStatusText = "Disconnected";
    
    /**
     * Creates a new SettingsDialog component.
     */
    public SettingsDialog() {
        // No FXML for this component, creating UI programmatically
        super("");
        
        // Create the main container which will be our overlay
        this.view = new StackPane();
        this.view.setId("settings-overlay");
        
        // Apply CSS stylesheet
        try {
            String cssPath = "/css/settings-dialog.css";
            var cssResource = getClass().getResource(cssPath);
            if (cssResource != null) {
                this.view.getStylesheets().add(cssResource.toExternalForm());
            } else {
                LOGGER.warning("Could not find CSS resource: " + cssPath);
                // Apply fallback styling directly
                this.view.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
            }
        } catch (Exception e) {
            LOGGER.warning("Error loading CSS for SettingsDialog: " + e.getMessage());
            // Apply fallback styling directly
            this.view.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        }
        
        // Set explicit alignment and positioning
        this.view.setAlignment(Pos.CENTER);
        
        // Create dialog content
        dialogContent = createDialogContent();
        
        // Add content to view with explicit center alignment
        StackPane.setAlignment(dialogContent, Pos.CENTER);
        this.view.getChildren().add(dialogContent);
        
        // Make the dialog take the full size of its parent
        this.view.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.view.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        // Apply explicit layout properties
        AnchorPane.setTopAnchor(this.view, 0.0);
        AnchorPane.setRightAnchor(this.view, 0.0);
        AnchorPane.setBottomAnchor(this.view, 0.0);
        AnchorPane.setLeftAnchor(this.view, 0.0);
        
        // Make sure the dialog sits on top of other elements
        this.view.setViewOrder(-100);
        
        // Close when clicking outside the dialog content
        this.view.setOnMouseClicked(event -> {
            if (event.getTarget() == this.view) {
                close();
                event.consume();
            }
        });
        
        // Start invisible
        this.view.setVisible(false);
        this.view.setManaged(false);
    }
    
    /**
     * Creates the content of the dialog.
     * 
     * @return A VBox containing the dialog content
     */
    private VBox createDialogContent() {
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);
        content.setMaxHeight(400);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 10;");
        
        // Prevent click-through to overlay
        content.setOnMouseClicked(event -> event.consume());
        
        // Add drop shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        content.setEffect(shadow);
        
        // Title
        Text title = new Text(SETTINGS_TITLE);
        title.setFont(Font.font("Cinzel", FontWeight.BOLD, 24));
        title.setFill(Color.GOLD);
        
        // Audio section
        VBox audioSection = createAudioSection();
        
        // Connection section
        VBox connectionSection = createConnectionSection();
        
        // Buttons row
        HBox buttonsRow = createButtonsRow();
        
        // Add all elements to the content
        content.getChildren().addAll(
                title,
                new Separator(),
                audioSection,
                new Separator(),
                connectionSection,
                new Separator(),
                buttonsRow
        );
        
        return content;
    }
    
    /**
     * Creates the audio settings section.
     * 
     * @return VBox containing audio settings controls
     */
    private VBox createAudioSection() {
        VBox audioSection = new VBox(10);
        audioSection.setAlignment(Pos.CENTER_LEFT);
        
        Label sectionTitle = new Label("Audio Settings");
        sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.WHITE);
        
        // Volume control
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
        
        // Mute checkbox
        HBox muteRow = new HBox(10);
        muteRow.setAlignment(Pos.CENTER_LEFT);
        
        Label muteLabel = new Label("Mute Audio:");
        muteLabel.setTextFill(Color.LIGHTGRAY);
        muteLabel.setMinWidth(80);
        
        CheckBox muteCheckbox = new CheckBox();
        muteCheckbox.selectedProperty().bindBidirectional(muteProperty);
        muteCheckbox.setTextFill(Color.LIGHTGRAY);
        
        // When mute is checked, disable the volume slider
        muteProperty.addListener((obs, oldVal, newVal) -> {
            volumeSlider.setDisable(newVal);
        });
        
        muteRow.getChildren().addAll(muteLabel, muteCheckbox);
        
        // Note about current audio implementation
        Label audioNoteLabel = new Label("Note: Audio is not yet implemented in this version.");
        audioNoteLabel.setTextFill(Color.GRAY);
        audioNoteLabel.setStyle("-fx-font-style: italic;");
        
        audioSection.getChildren().addAll(sectionTitle, volumeRow, muteRow, audioNoteLabel);
        return audioSection;
    }
    
    /**
     * Creates the connection status section.
     * 
     * @return VBox containing connection status information
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
     * @return HBox containing dialog buttons
     */
    private HBox createButtonsRow() {
        HBox buttonsRow = new HBox(10);
        buttonsRow.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveButton = new Button("Save Settings");
        saveButton.getStyleClass().add("settings-button");
        saveButton.getStyleClass().add("save-button");
        // Add fallback styling
        saveButton.setStyle("-fx-background-color: #4a7a28; -fx-text-fill: #e8e8e8; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 5;");
        saveButton.setOnAction(e -> {
            if (onSaveAction != null) {
                onSaveAction.run();
            }
            close();
        });
        
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("settings-button");
        cancelButton.setStyle("-fx-background-color: #4a3f35; -fx-text-fill: #e8e8e8; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 5;");
        cancelButton.setOnAction(e -> {
            close();
        });
        
        buttonsRow.getChildren().addAll(cancelButton, saveButton);
        return buttonsRow;
    }
    
    /**
     * Shows the settings dialog with a fade-in animation.
     */
    public void show() {
        // Make visible and managed before animation
        this.view.setVisible(true);
        this.view.setManaged(true);
        
        // Reset opacity in case it was previously animated
        this.view.setOpacity(0);
        
        // Make sure the dialog sits on top
        this.view.toFront();
        
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), this.view);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    /**
     * Closes the settings dialog with a fade-out animation.
     */
    public void close() {
        // Fade out animation
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
     * @param connected Whether the client is connected to the server
     * @param statusText Status text to display
     */
    public void setConnectionStatus(boolean connected, String statusText) {
        this.isConnected = connected;
        this.connectionStatusText = statusText;
        
        // If the dialog is already built, update the UI directly
        if (this.view.isVisible()) {
            // Find and update the connection status label
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
     * @return SimpleDoubleProperty for volume
     */
    public SimpleDoubleProperty volumeProperty() {
        return volumeProperty;
    }
    
    /**
     * Gets the mute property.
     * 
     * @return BooleanProperty for mute status
     */
    public BooleanProperty muteProperty() {
        return muteProperty;
    }
    
    /**
     * Sets an action to be executed when the dialog is closed.
     * 
     * @param action The action to execute on close
     */
    public void setOnCloseAction(Runnable action) {
        this.onCloseAction = action;
    }
    
    /**
     * Sets an action to be executed when the save button is clicked.
     * 
     * @param action The action to execute on save
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
