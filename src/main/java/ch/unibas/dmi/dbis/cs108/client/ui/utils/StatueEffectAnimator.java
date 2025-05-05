package ch.unibas.dmi.dbis.cs108.client.ui.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.logging.Logger;

/**
 * Utility class for creating animations for statue effects.
 * This provides reusable animations for all statue types,
 * with specific implementations for Freyr's tree growing.
 */
public class StatueEffectAnimator {
    private static final Logger LOGGER = Logger.getLogger(StatueEffectAnimator.class.getName());

    /**
     * Creates and plays a tree growing animation at the specified location.
     * 
     * @param parent   The parent pane for the animation
     * @param x        The x coordinate on screen
     * @param y        The y coordinate on screen
     * @param onFinish Runnable to execute after animation completes
     */
    public static void playTreeGrowingAnimation(Pane parent, double x, double y, Runnable onFinish) {
        // Create a simple growing circle animation
        Circle circle = new Circle(0, Color.GREEN);
        circle.setCenterX(x);
        circle.setCenterY(y);
        circle.setOpacity(0.7);

        // Add a glow effect
        Glow glow = new Glow();
        glow.setLevel(0.5);
        circle.setEffect(glow);

        parent.getChildren().add(circle);

        // Create the grow animation
        Timeline growTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(circle.radiusProperty(), 0)),
                new KeyFrame(Duration.millis(1000), new KeyValue(circle.radiusProperty(), 40)));

        // Create fade out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), circle);
        fadeOut.setFromValue(0.7);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            parent.getChildren().remove(circle);
            if (onFinish != null) {
                onFinish.run();
            }
        });

        // Play animations in sequence
        growTimeline.setOnFinished(e -> fadeOut.play());
        growTimeline.play();
    }

    /**
     * Creates and plays a statue upgrade animation.
     * 
     * @param statue   The statue node to animate
     * @param onFinish Runnable to execute after animation completes
     */
    public static void playStatueUpgradeAnimation(Node statue, Runnable onFinish) {
        // Add a glow effect
        Glow glow = new Glow();
        glow.setLevel(0);
        statue.setEffect(glow);

        // Create the grow animation
        Timeline glowTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0)),
                new KeyFrame(Duration.millis(1000), new KeyValue(glow.levelProperty(), 0.8)),
                new KeyFrame(Duration.millis(2000), new KeyValue(glow.levelProperty(), 0)));

        // Add scaling animation
        double originalScaleX = statue.getScaleX();
        double originalScaleY = statue.getScaleY();

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(500), statue);
        scaleUp.setToX(originalScaleX * 1.2);
        scaleUp.setToY(originalScaleY * 1.2);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(500), statue);
        scaleDown.setToX(originalScaleX);
        scaleDown.setToY(originalScaleY);

        // Play animations in sequence
        SequentialTransition sequence = new SequentialTransition(
                scaleUp,
                scaleDown);

        ParallelTransition allAnimations = new ParallelTransition(
                glowTimeline,
                sequence);

        allAnimations.setOnFinished(e -> {
            statue.setEffect(null);
            if (onFinish != null) {
                onFinish.run();
            }
        });

        allAnimations.play();
    }

    /**
     * Creates and plays a blessing animation for a statue.
     * 
     * @param parent   The parent pane for the animation
     * @param statue   The statue node
     * @param color    The color of the blessing effect
     * @param onFinish Runnable to execute after animation completes
     */
    public static void playBlessingAnimation(Pane parent, Node statue, Color color, Runnable onFinish) {
        // Create rays emanating from the statue
        double centerX = statue.getLayoutX() + statue.getBoundsInLocal().getWidth() / 2;
        double centerY = statue.getLayoutY() + statue.getBoundsInLocal().getHeight() / 2;

        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4; // 8 rays evenly distributed

            Rectangle ray = new Rectangle(5, 0);
            ray.setFill(color);
            ray.setX(centerX);
            ray.setY(centerY);
            ray.setRotate(Math.toDegrees(angle));

            parent.getChildren().add(ray);

            // Animate the ray growing outward
            Timeline rayGrow = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(ray.heightProperty(), 0)),
                    new KeyFrame(Duration.millis(1000), new KeyValue(ray.heightProperty(), 100)));

            // Fade out
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), ray);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setDelay(Duration.millis(1000));
            fadeOut.setOnFinished(e -> parent.getChildren().remove(ray));

            ParallelTransition rayAnimation = new ParallelTransition(rayGrow, fadeOut);
            rayAnimation.play();
        }

        // Add a bloom effect to the statue
        Bloom bloom = new Bloom();
        bloom.setThreshold(0.1);
        statue.setEffect(bloom);

        // Animate the bloom effect
        Timeline bloomTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(bloom.thresholdProperty(), 0.1)),
                new KeyFrame(Duration.millis(1000), new KeyValue(bloom.thresholdProperty(), 0.7)),
                new KeyFrame(Duration.millis(2000), new KeyValue(bloom.thresholdProperty(), 0.1)));

        bloomTimeline.setOnFinished(e -> {
            statue.setEffect(null);
            if (onFinish != null) {
                onFinish.run();
            }
        });

        bloomTimeline.play();
    }

    /**
     * Creates and plays a curse animation for a statue.
     * 
     * @param parent   The parent pane for the animation
     * @param statue   The statue node
     * @param onFinish Runnable to execute after animation completes
     */
    public static void playCurseAnimation(Pane parent, Node statue, Runnable onFinish) {
        // Add a red glow/shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.RED);
        shadow.setRadius(0);
        statue.setEffect(shadow);

        // Animate the shadow
        Timeline shadowTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(shadow.radiusProperty(), 0)),
                new KeyFrame(Duration.millis(1000), new KeyValue(shadow.radiusProperty(), 20)),
                new KeyFrame(Duration.millis(2000), new KeyValue(shadow.radiusProperty(), 0)));

        // Add some shake animation
        double originalX = statue.getLayoutX();
        double originalY = statue.getLayoutY();

        Timeline shakeTimeline = new Timeline();
        for (int i = 0; i < 10; i++) {
            double offset = (i % 2 == 0) ? 5 : -5;
            shakeTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(i * 100),
                            new KeyValue(statue.layoutXProperty(), originalX + offset)));
        }
        shakeTimeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(1000), new KeyValue(statue.layoutXProperty(), originalX)));

        ParallelTransition animation = new ParallelTransition(shadowTimeline, shakeTimeline);
        animation.setOnFinished(e -> {
            statue.setEffect(null);
            if (onFinish != null) {
                onFinish.run();
            }
        });

        animation.play();
    }
}
