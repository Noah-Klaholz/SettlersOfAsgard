package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SplashScreenController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(SplashScreenController.class.getName());

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

        // Start animations
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
        gameLogo.setOpacity(0);
        gameLogo.setScaleX(0.5);
        gameLogo.setScaleY(0.5);
        titleLabel.setOpacity(0);

        // Logo animations
        FadeTransition logoFade = new FadeTransition(Duration.seconds(1.5), gameLogo);
        logoFade.setFromValue(0);
        logoFade.setToValue(1);

        ScaleTransition logoScale = new ScaleTransition(Duration.seconds(2), gameLogo);
        logoScale.setFromX(0.5);
        logoScale.setFromY(0.5);
        logoScale.setToX(1.0);
        logoScale.setToY(1.0);

        // Title animation
        FadeTransition titleFade = new FadeTransition(Duration.seconds(1.5), titleLabel);
        titleFade.setFromValue(0);
        titleFade.setToValue(1);
        titleFade.setDelay(Duration.seconds(0.8));

        // Play all animations together
        ParallelTransition parallel = new ParallelTransition(logoFade, logoScale, titleFade);

        // After animations, switch to main menu
        parallel.setOnFinished(e -> {
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(event -> sceneManager.switchToScene(SceneManager.SceneType.MAIN_MENU));
            delay.play();
        });

        parallel.play();
    }
}