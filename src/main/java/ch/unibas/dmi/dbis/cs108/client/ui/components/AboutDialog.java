package ch.unibas.dmi.dbis.cs108.client.ui.components;

import ch.unibas.dmi.dbis.cs108.client.audio.AudioManager;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.StylesheetLoader;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.scene.control.Hyperlink;
import java.awt.Desktop;
import java.net.URI;


import java.util.logging.Logger;

/**
 * A reusable About dialog component that displays information about the game.
 * This component creates a modal dialog with game information, credits, and
 * version.
 */
public class AboutDialog extends UIComponent<StackPane> {
    /**
     * Logger for the AboutDialog class.
     */
    private static final Logger LOGGER = Logger.getLogger(AboutDialog.class.getName());
    /**
     * The title of the game.
     */
    private static final String GAME_TITLE = "Settlers of Asgard";
    /**
     * The version of the game.
     */
    private static final String GAME_VERSION = "Version 1.0.0";
    /**
     * The description of the game.
     */
    private static final String GAME_DESCRIPTION = "Settlers of Asgard is a strategic board game where players compete to build "
            + "the most powerful settlement in the mythical Norse realm of Asgard. "
            + "Gather resources, construct buildings, and earn the favor of the gods to win!";

    /* * The development team information.
     */
    private static final String DEVELOPMENT_TEAM = "Developed by Gruppe-3 for the Programming Project at University of Basel.";
    /**
     * The copyright information.
     */
    private static final String COPYRIGHT = "© 2024 University of Basel";

    /**
     * The view of the dialog.
     */
    private final VBox dialogContent;

    /**
     * Creates a new AboutDialog component.
     */
    public AboutDialog() {
        super("");
        this.view = new StackPane();
        this.view.setId("about-overlay");
        this.view.getStyleClass().add("dialog-overlay"); // Add style class

        // Load stylesheets using the StylesheetLoader utility
        StylesheetLoader.loadDialogStylesheets(this.view);
        StylesheetLoader.loadStylesheet(this.view, "/css/about-dialog.css");

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

        AudioManager.attachClickSoundToAllButtons(this.view);
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

        Text title = new Text(GAME_TITLE);
        title.getStyleClass().add("dialog-title"); // Use style class

        Label version = new Label(GAME_VERSION);
        version.getStyleClass().add("dialog-label"); // Use style class

        Text description = new Text(GAME_DESCRIPTION);
        description.getStyleClass().add("dialog-text"); // Use style class
        description.setTextAlignment(TextAlignment.CENTER); // Keep alignment
        description.setWrappingWidth(450); // Keep wrapping width

        Text team = new Text(DEVELOPMENT_TEAM);
        team.getStyleClass().add("dialog-text"); // Use style class
        team.setTextAlignment(TextAlignment.CENTER);
        team.setWrappingWidth(450);

        Label copyright = new Label(COPYRIGHT);
        copyright.getStyleClass().add("dialog-note"); // Use style class

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().addAll("dialog-button", "dialog-button-cancel"); // Use style classes
        closeButton.setOnAction(e -> {
            close();
            e.consume();
        });

        Hyperlink websiteLink = new Hyperlink("Visit our Website!");
        websiteLink.getStyleClass().add("dialog-link");
        websiteLink.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://settlersofasgard.netlify.app"));
            } catch (Exception ex) {
                LOGGER.warning("Failed to open link: " + ex.getMessage());
            }
        });


        content.getChildren().addAll(
                title,
                version,
                new DialogSeparator(), // Use custom styled separator
                description,
                new DialogSeparator(),
                team,
                new DialogSeparator(),
                websiteLink,
                copyright,
                closeButton);
        return content;
    }

    /**
     * Shows the about dialog with a fade-in animation.
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
     * Closes the about dialog with a fade-out animation.
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
     * Sets an action to be executed when the dialog is closed.
     * This method now correctly calls the superclass setter.
     *
     * @param action The action to execute on close.
     */
    @Override
    public void setOnCloseAction(Runnable action) {
        super.setOnCloseAction(action); // Call superclass method
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
