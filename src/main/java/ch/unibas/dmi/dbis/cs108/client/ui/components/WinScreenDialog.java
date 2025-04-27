package ch.unibas.dmi.dbis.cs108.client.ui.components;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WinScreenDialog is a UI component that displays a dialog with the game
 */
public class WinScreenDialog extends UIComponent<StackPane> {

    /** The runnable onMenuAction that takes you back to the main Menu */
    private Runnable onMenuAction;
    /** The runnable onLobbyAction that takes you back to the lobby */
    private Runnable onLobbyAction;

    /**
     * Constructor for WinScreenDialog.
     *
     * @param leaderboard A map containing player names and their scores.
     */
    public WinScreenDialog(Map<String, Integer> leaderboard) {
        super("");
        StackPane root = getView();
        root.getStyleClass().addAll("overlay", "dialog-background");

        VBox container = new VBox(24);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("dialog-container");

        Label title = new Label("Game Over");
        title.getStyleClass().add("dialog-section-title");

        // Sort leaderboard descending
        List<Map.Entry<String, Integer>> sorted = leaderboard.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        VBox boardBox = new VBox(8);
        boardBox.setAlignment(Pos.CENTER_LEFT);
        for (Map.Entry<String, Integer> entry : sorted) {
            Label row = new Label(entry.getKey() + ": " + entry.getValue());
            row.getStyleClass().add("dialog-label");
            boardBox.getChildren().add(row);
        }

        HBox buttons = new HBox(16);
        buttons.setAlignment(Pos.CENTER);
        Button mainMenu = new Button("Main Menu");
        mainMenu.getStyleClass().addAll("dialog-button", "dialog-button-save");
        mainMenu.setOnAction(evt -> {
            if (onMenuAction != null) onMenuAction.run();
            close();
        });

        Button backLobby = new Button("Back to Lobby");
        backLobby.getStyleClass().addAll("dialog-button", "dialog-button-cancel");
        backLobby.setOnAction(evt -> {
            if (onLobbyAction != null) onLobbyAction.run();
            close();
        });

        buttons.getChildren().addAll(mainMenu, backLobby);
        container.getChildren().addAll(title, boardBox, buttons);
        root.getChildren().add(container);
    }

    /**
     * Shows the dialog with a fade-in effect.
     */
    @Override
    public void show() {
        getView().setVisible(true);
        getView().setManaged(true);
        getView().setOpacity(0);
        getView().toFront();
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), getView());
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    /**
     * Closes the dialog with a fade-out effect.
     */
    public void close() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), getView());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            getView().setVisible(false);
            getView().setManaged(false);
            if (getView().getParent() instanceof StackPane parent) {
                parent.getChildren().remove(getView());
            }
            Runnable onClose = getOnCloseAction();
            if (onClose != null) onClose.run();
        });
        fadeOut.play();
    }

    /**
     * Sets the action to be performed when the menu button is clicked.
     *
     * @param action The action to be performed.
     */
    public void setOnMenuAction(Runnable action) {
        this.onMenuAction = action;
    }

    /**
     * Sets the action to be performed when the lobby button is clicked.
     *
     * @param action The action to be performed.
     */
    public void setOnLobbyAction(Runnable action) {
        this.onLobbyAction = action;
    }
}