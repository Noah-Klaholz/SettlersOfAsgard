package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * TimerComponent is a UI component that displays a countdown timer for player turns.
 * It updates the displayed time every second and resets when the player changes.
 */
public class TimerComponent extends StackPane {
    /**
     * Default number of seconds for the timer.
     */
    private static final int DEFAULT_SECONDS = SETTINGS.Config.TURN_TIME.getValue();
    /**
     * Default time format for displaying the timer.
     */
    private final Label timerLabel = new Label();
    /**
     * The number of seconds left in the timer.
     */
    private int secondsLeft = DEFAULT_SECONDS;
    /**
     * The timeline for the countdown animation.
     */
    private Timeline timeline;
    /**
     * The last player turn that was active.
     */
    private String lastPlayerTurn = null;

    /**
     * Constructor for TimerComponent.
     * Initializes the timer label and sets its style.
     */
    public TimerComponent() {
        timerLabel.setText(formatTime(secondsLeft));
        timerLabel.getStyleClass().add("turn-timer-label");
        setAlignment(Pos.CENTER);
        getChildren().add(timerLabel);
        getStyleClass().add("turn-timer-container");
    }

    /**
     * Starts the countdown timer for the specified player turn.
     *
     * @param currentPlayerTurn The player whose turn is currently active.
     */
    public void start(String currentPlayerTurn) {
        if (timeline != null) {
            timeline.stop();
        }
        this.lastPlayerTurn = currentPlayerTurn;
        secondsLeft = DEFAULT_SECONDS;
        updateLabel();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsLeft--;
            updateLabel();
            if (secondsLeft <= 0) {
                timeline.stop();
            }
        }));
        timeline.setCycleCount(DEFAULT_SECONDS);
        timeline.playFromStart();
    }

    /**
     * Resets the timer if the player has changed.
     *
     * @param newPlayerTurn The new player whose turn is active.
     */
    public void resetIfPlayerChanged(String newPlayerTurn) {
        if (lastPlayerTurn == null || !lastPlayerTurn.equals(newPlayerTurn)) {
            start(newPlayerTurn);
        }
    }

    /**
     * Stops the countdown timer.
     */
    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    /**
     * Resets the timer to the default value.
     */
    private void updateLabel() {
        Platform.runLater(() -> timerLabel.setText(formatTime(secondsLeft)));
    }

    /**
     * Formats the time in seconds to a string in the format mm:ss.
     *
     * @param seconds The number of seconds to format.
     * @return The formatted time string.
     */
    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
