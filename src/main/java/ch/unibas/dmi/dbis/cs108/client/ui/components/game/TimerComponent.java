package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class TimerComponent extends StackPane {
    private static final int DEFAULT_SECONDS = SETTINGS.Config.TURN_TIME.getValue();
    private int secondsLeft = DEFAULT_SECONDS;
    private final Label timerLabel = new Label();
    private Timeline timeline;
    private String lastPlayerTurn = null;

    public TimerComponent() {
        timerLabel.setText(formatTime(secondsLeft));
        timerLabel.getStyleClass().add("turn-timer-label");
        setAlignment(Pos.CENTER);
        getChildren().add(timerLabel);
        getStyleClass().add("turn-timer-container");
    }

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

    public void resetIfPlayerChanged(String newPlayerTurn) {
        if (lastPlayerTurn == null || !lastPlayerTurn.equals(newPlayerTurn)) {
            start(newPlayerTurn);
        }
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void updateLabel() {
        Platform.runLater(() -> timerLabel.setText(formatTime(secondsLeft)));
    }

    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
