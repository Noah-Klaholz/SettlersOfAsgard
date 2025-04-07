// File: src/main/java/ch/unibas/dmi/dbis/cs108/client/ui/controllers/MainMenuController.java
package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import javafx.fxml.FXML;

import java.util.logging.Logger;

public class MainMenuController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());

    public MainMenuController() {
        super(new ResourceLoader(), UIEventBus.getInstance(), SceneManager.getInstance());
    }

    @FXML
    private void initialize() {
        LOGGER.info("Main menu initialized");
    }

    @FXML
    private void handlePlayGame() {
        LOGGER.info("Play button clicked");
        sceneManager.switchToScene(SceneManager.SceneType.GAME);
    }
}