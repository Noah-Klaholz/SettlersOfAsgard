package ch.unibas.dmi.dbis.cs108.client.ui.components;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.StylesheetLoader;
import javafx.animation.FadeTransition;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    /** The dialog content */
    private final VBox dialogContent;

    /**
     * Constructor for WinScreenDialog.
     *
     * @param leaderboard A map containing player names and their scores.
     */
    public WinScreenDialog(Map<String, Integer> leaderboard) {
        super("");
        this.view = new StackPane();
        this.view.getStyleClass().add("dialog-overlay");

        StylesheetLoader.loadDialogStylesheets(this.view);
        StylesheetLoader.loadStylesheet(this.view, "/css/winscreen-dialog.css");

        this.view.setAlignment(Pos.CENTER);
        dialogContent = createDialogContent(leaderboard);
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
     * Creates the dialog content with a title, leaderboard, and buttons.
     *
     * @return The VBox containing the dialog content.
     */
    private VBox createDialogContent(Map<String, Integer> leaderboard) {
        VBox dialogContent = new VBox(20);
        dialogContent.getStyleClass().add("dialog-content");
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setOnMouseClicked(Event::consume);

        Label titleLabel = new Label("Game Over");
        titleLabel.getStyleClass().add("dialog-title");

        LeaderboardView leaderboardBox = new LeaderboardView(new HashMap<>(leaderboard));

        Button menuButton = new Button("Main Menu");
        menuButton.getStyleClass().add("menu-button");
        menuButton.setOnAction(e -> {
            if (onMenuAction != null) onMenuAction.run();
            close();
        });

        Button lobbyButton = new Button("Lobby");
        lobbyButton.getStyleClass().add("lobby-button");
        lobbyButton.setOnAction(e -> {
            if (onLobbyAction != null) onLobbyAction.run();
            close();
        });

        HBox buttonBox = new HBox(menuButton, lobbyButton);
        buttonBox.getStyleClass().add("button-box");
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);

        dialogContent.getChildren().addAll(
                titleLabel,
                new DialogSeparator(),
                leaderboardBox,
                new DialogSeparator(),
                buttonBox);

        return dialogContent;
    }

    /**
     * Shows the dialog with a fade-in effect.
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
     * Closes the dialog with a fade-out effect.
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

    /**
     * Custom separator using CSS styling.
     */
    private static class DialogSeparator extends Region {
        public DialogSeparator() {
            getStyleClass().add("dialog-separator"); // Use style class
        }
    }

    /**
     * Custom leaderboard view to display player scores.
     */
    public class LeaderboardView extends VBox {

        /**
         * Constructor for LeaderboardView.
         *
         * @param leaderboard A map containing player names and their scores.
         */
        public LeaderboardView(HashMap<String, Integer> leaderboard) {
            setSpacing(8);
            setAlignment(Pos.CENTER);

            // Sort players by score descending
            List<Map.Entry<String, Integer>> sorted = leaderboard.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                    .toList();

            int rank = 1;
            for (Map.Entry<String, Integer> entry : sorted) {
                HBox row = new HBox(20);
                row.setAlignment(Pos.CENTER_LEFT);

                Label name = new Label(entry.getKey());
                Label score = new Label(String.valueOf(entry.getValue()));

                // Assign style class based on rank
                if (rank == 1) {
                    row.getStyleClass().add("leaderboard-gold");
                } else if (rank == 2) {
                    row.getStyleClass().add("leaderboard-silver");
                } else if (rank == 3) {
                    row.getStyleClass().add("leaderboard-bronze");
                } else {
                    row.getStyleClass().add("leaderboard-darkblue");
                }

                row.getChildren().addAll(new Label("#" + rank), name, score);
                getChildren().add(row);
                rank++;
            }
        }
    }
}