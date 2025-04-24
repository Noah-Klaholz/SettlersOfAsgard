package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.BlendMode;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.animation.SequentialTransition;
import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.scene.shape.Circle;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SplashScreenController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(SplashScreenController.class.getName());
    private static final int duration = SETTINGS.Config.SPLASH_SCREEN_DURATION.getValue();

    @FXML private StackPane splashRoot;
    @FXML private ImageView gameLogo;
    @FXML private Label titleLabel;

    public SplashScreenController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
    }

    @FXML
    private void initialize() {
        LOGGER.info("Initializing splash screen...");
        loadGameLogo();

        // Start animations - logo is already at full size
        playIntroAnimations();
    }

    private void loadGameLogo() {
        try {
            Image logo = resourceLoader.loadImage(ResourceLoader.GAME_LOGO);
            if (logo != null) {
                gameLogo.setImage(logo);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Couldn't load game logo", e);
        }
    }

    private void playIntroAnimations() {
        // Initial setup - hide elements
        titleLabel.setOpacity(0);

        // Create a white rectangle for the light effect
        Rectangle lightStrip = new Rectangle();
        lightStrip.setWidth(50);  // Narrower strip for more focused reflection
        lightStrip.setHeight(gameLogo.getFitHeight() * 1.5);
        lightStrip.setFill(new Color(1, 1, 1, 0.7));  // Slightly more transparent
        lightStrip.setBlendMode(BlendMode.ADD);
        lightStrip.setRotate(15);  // Angled light reflection

        // Position it properly
        StackPane.setAlignment(lightStrip, Pos.CENTER);
        splashRoot.getChildren().add(lightStrip);

        // Create a proper clip that matches the actual image, not just a circle
        javafx.scene.shape.Rectangle imageClip = new javafx.scene.shape.Rectangle(
                gameLogo.getFitWidth(),
                gameLogo.getFitHeight()
        );
        imageClip.setArcWidth(20);  // Rounded corners if needed
        imageClip.setArcHeight(20);

        // Bind the clip to the actual image view position and size
        imageClip.xProperty().bind(gameLogo.layoutXProperty());
        imageClip.yProperty().bind(gameLogo.layoutYProperty());

        // Apply the clip to the light strip
        lightStrip.setClip(imageClip);

        // Animate the light strip moving across the logo
        TranslateTransition lightMove = new TranslateTransition(Duration.seconds((double) duration / 3), lightStrip);
        lightMove.setFromX(-gameLogo.getFitWidth() - 100);
        lightMove.setToX(gameLogo.getFitWidth() + 100);

        // Add a second reflection pass
        TranslateTransition lightMoveReturn = new TranslateTransition(Duration.seconds((double) duration / 3), lightStrip);
        lightMoveReturn.setFromX(gameLogo.getFitWidth() + 100);
        lightMoveReturn.setToX(-gameLogo.getFitWidth() - 100);

        // Title animation
        FadeTransition titleFade = new FadeTransition(Duration.seconds((double) duration / 3), titleLabel);
        titleFade.setFromValue(0);
        titleFade.setToValue(1);
        titleFade.setDelay(Duration.seconds(0.8));

        // Create a sequence with both light passes
        SequentialTransition sequence = new SequentialTransition(
                lightMove,
                new PauseTransition(Duration.seconds(0.5)),
                lightMoveReturn
        );

        sequence.setOnFinished(e -> {
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(event -> {
                sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU);

                // Request focus after scene switch
                Platform.runLater(() -> {
                    // Small delay to ensure scene is fully loaded
                    PauseTransition focusDelay = new PauseTransition(Duration.millis(100));
                    focusDelay.setOnFinished(focusEvent -> {
                        Scene currentScene = splashRoot.getScene();
                        if (currentScene != null) {
                            currentScene.getWindow().requestFocus();
                            currentScene.getRoot().requestFocus();
                        }
                    });
                    focusDelay.play();
                });
            });
            delay.play();
        });

        sequence.play();
        titleFade.play();
    }
}