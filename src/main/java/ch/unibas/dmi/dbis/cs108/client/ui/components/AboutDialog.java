package ch.unibas.dmi.dbis.cs108.client.ui.components;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import java.util.logging.Logger;

/**
 * A reusable About dialog component that displays information about the game.
 * This component creates a modal dialog with game information, credits, and version.
 */
public class AboutDialog extends UIComponent<StackPane> {
    private static final Logger LOGGER = Logger.getLogger(AboutDialog.class.getName());
    private static final String GAME_TITLE = "Settlers of Asgard";
    private static final String GAME_VERSION = "Version 1.0.0";
    private static final String GAME_DESCRIPTION = 
            "Settlers of Asgard is a strategic board game where players compete to build "
            + "the most powerful settlement in the mythical Norse realm of Asgard. "
            + "Gather resources, construct buildings, and earn the favor of the gods to win!";
    private static final String DEVELOPMENT_TEAM = 
            "Developed by Gruppe-3 for the Programming Project at University of Basel.";
    private static final String COPYRIGHT = "© 2023 University of Basel";
    
    private final VBox dialogContent;
    private Runnable onCloseAction;
    
    /**
     * Creates a new AboutDialog component.
     */
    public AboutDialog() {
        // No FXML for this component, creating UI programmatically
        super("");
        
        // Create the main container which will be our overlay
        this.view = new StackPane();
        this.view.setId("about-overlay");
        
        // Apply CSS stylesheet
        try {
            String cssPath = "/css/about-dialog.css";
            var cssResource = getClass().getResource(cssPath);
            if (cssResource != null) {
                this.view.getStylesheets().add(cssResource.toExternalForm());
            } else {
                LOGGER.warning("Could not find CSS resource: " + cssPath);
                // Apply fallback styling directly
                this.view.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-alignment: center;");
            }
        } catch (Exception e) {
            LOGGER.warning("Error loading CSS for AboutDialog: " + e.getMessage());
            // Apply fallback styling directly
            this.view.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-alignment: center;");
        }
        
        // Set alignment for the StackPane to center its children
        this.view.setAlignment(Pos.CENTER);
        
        // Create dialog content
        dialogContent = createDialogContent();
        
        // Add content to view and ensure proper centering
        StackPane.setAlignment(dialogContent, Pos.CENTER);
        this.view.getChildren().add(dialogContent);
        
        // Make the dialog take the full size of its parent
        this.view.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
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
        Text title = new Text(GAME_TITLE);
        title.setFont(Font.font("Cinzel", FontWeight.BOLD, 28));
        title.setFill(Color.GOLD);
        
        // Version
        Label version = new Label(GAME_VERSION);
        version.setTextFill(Color.LIGHTGRAY);
        
        // Description
        Text description = new Text(GAME_DESCRIPTION);
        description.setFont(Font.font("Roboto", 14));
        description.setFill(Color.WHITE);
        description.setTextAlignment(TextAlignment.CENTER);
        description.setWrappingWidth(450);
        
        // Team info
        Text team = new Text(DEVELOPMENT_TEAM);
        team.setFont(Font.font("Roboto", 12));
        team.setFill(Color.LIGHTGRAY);
        team.setTextAlignment(TextAlignment.CENTER);
        team.setWrappingWidth(450);
        
        // Copyright
        Label copyright = new Label(COPYRIGHT);
        copyright.setTextFill(Color.GRAY);
        
        // Close button
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("menu-button");
        // Add fallback styling
        closeButton.setStyle("-fx-background-color: #4a3f35; -fx-text-fill: #e8e8e8; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 5;");
        closeButton.setOnAction(e -> {
            close();
            e.consume(); // Make sure the event is consumed
        });
        
        // Add all elements to the content
        content.getChildren().addAll(
                title,
                version,
                new Separator(),
                description,
                new Separator(),
                team,
                copyright,
                closeButton
        );
        
        return content;
    }
    
    /**
     * Shows the about dialog with a fade-in animation.
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
     * Closes the about dialog with a fade-out animation.
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
     * Sets an action to be executed when the dialog is closed.
     * 
     * @param action The action to execute on close
     */
    public void setOnCloseAction(Runnable action) {
        this.onCloseAction = action;
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
