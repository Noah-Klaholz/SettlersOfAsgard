package ch.unibas.dmi.dbis.cs108.client.ui.components;

import ch.unibas.dmi.dbis.cs108.server.core.model.Leaderboard;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.StylesheetLoader;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LeaderboardDialog is a UI component that represents a dialog for displaying
 */
public class LeaderboardDialog extends UIComponent<StackPane> {
    /** The title of the leaderboard dialog */
    private static final String LEADERBOARD_TITLE = "Global Leaderboard";
    /** The leaderboard  */
    private Leaderboard leaderboard;
    /** The VBox for all entries in the dialog */
    private VBox leaderboardBox;
    /** The search field for the highScore List */
    private TextField searchField;

    /**
     * Constructor for the LeaderboardDialog.
     * Initializes the dialog with a title and sets up the layout and styles.
     */
    public LeaderboardDialog() {
        super("");
        this.view = new StackPane();
        this.view.setId("leaderboard-overlay");
        this.view.getStyleClass().add("dialog-overlay");

        StylesheetLoader.loadDialogStylesheets(this.view);
        StylesheetLoader.loadStylesheet(this.view, "/css/leaderboard-dialog.css");

        this.view.setAlignment(Pos.CENTER);
        VBox dialogContent = createDialogContent();
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
     * Creates the content of the leaderboard dialog.
     *
     * @return VBox containing the dialog content
     */
    private VBox createDialogContent() {
        VBox content = new VBox(15);
        content.getStyleClass().add("dialog-content-box");
        content.setAlignment(Pos.CENTER);
        content.setOnMouseClicked(e -> e.consume());
        content.setPadding(new Insets(30, 30, 30, 30));
        content.setMaxWidth(400);

        Text title = new Text(LEADERBOARD_TITLE);
        title.getStyleClass().add("dialog-title");

        searchField = new TextField();
        searchField.setPromptText("Search player...");
        searchField.getStyleClass().add("dialog-textfield");
        searchField.setMaxWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateLeaderboardEntries());

        leaderboardBox = new VBox(5);
        leaderboardBox.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(leaderboardBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setMaxHeight(300);
        scrollPane.getStyleClass().add("dialog-scrollpane");

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().addAll("dialog-button", "dialog-button-cancel");
        closeButton.setOnAction(e -> close());

        content.getChildren().addAll(
                title,
                new DialogSeparator(),
                searchField,
                scrollPane,
                new DialogSeparator(),
                closeButton
        );
        return content;
    }

    /**
     * Updates the leaderboard entries in the dialog based on the current leaderboard data.
     * Filters the entries based on the search field input and sorts them by score.
     */
    private void updateLeaderboardEntries() {
        leaderboardBox.getChildren().clear();
        if (leaderboard == null) return;

        String filter = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        Map<String, Integer> entries = leaderboard.getLeaderboard();
        List<Map.Entry<String, Integer>> sorted = entries.entrySet().stream()
                .filter(e -> filter.isEmpty() || e.getKey().toLowerCase().contains(filter))
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .toList();

        int rank = 1;
        for (Map.Entry<String, Integer> entry : sorted) {
            leaderboardBox.getChildren().add(createLeaderboardCell(rank, entry.getKey(), entry.getValue()));
            rank++;
        }
    }

    /**
     * Creates a cell for the leaderboard entry.
     *
     * @param rank The rank of the player
     * @param name The name of the player
     * @param score The score of the player
     * @return HBox representing the leaderboard cell
     */
    private HBox createLeaderboardCell(int rank, String name, int score) {
        HBox cell = new HBox(10);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(8, 12, 8, 12));
        cell.setMinHeight(40);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.getStyleClass().add("leaderboard-cell");

        Label rankLabel = new Label(String.valueOf(rank));
        rankLabel.getStyleClass().add("leaderboard-rank");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("leaderboard-name");

        Label scoreLabel = new Label(String.valueOf(score));
        scoreLabel.getStyleClass().add("leaderboard-score");

        // Special styling for top 3
        if (rank == 1) {
            rankLabel.setStyle("-fx-text-fill: #000000;");
            cell.getStyleClass().add("leaderboard-gold");
        } else if (rank == 2) {
            rankLabel.setStyle("-fx-text-fill: #000000;");
            cell.getStyleClass().add("leaderboard-silver");
        } else if (rank == 3) {
            rankLabel.setStyle("-fx-text-fill: #000000;");
            cell.getStyleClass().add("leaderboard-bronze");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        cell.getChildren().addAll(rankLabel, nameLabel, spacer, scoreLabel);
        return cell;
    }

    /**
     * Shows the leaderboard dialog.
     */
    @Override
    public void show() {
        updateLeaderboardEntries();
        this.view.setVisible(true);
        this.view.setManaged(true);
        this.view.setOpacity(0);
        this.view.toFront();
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), this.view);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        Platform.runLater(() -> searchField.requestFocus());
    }

    /**
     * Closes the leaderboard dialog.
     */
    public void close() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), this.view);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            this.view.setVisible(false);
            this.view.setManaged(false);
            if (this.view.getParent() instanceof Pane parentPane) {
                parentPane.getChildren().remove(this.view);
            }
            Runnable action = getOnCloseAction();
            if (action != null) {
                action.run();
            }
        });
        fadeOut.play();
    }

    /**
     * Sets the leaderboard data for the dialog and updates the entries.
     *
     * @param leaderboard
     */
    public void setLeaderboard(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
        updateLeaderboardEntries();
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
