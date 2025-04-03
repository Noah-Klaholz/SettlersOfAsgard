//package ch.unibas.dmi.dbis.cs108.client.app;
//
//import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
//import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.PingFilter;
//import javafx.application.Application;
//import javafx.stage.Stage;
//
//import java.util.logging.Logger;
//
//public class ClientMainApp extends Application {
//    private static final Logger logger = Logger.getLogger(ClientMainApp.class.getName());
//    private SceneManager sceneManager;
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    @Override
//    public void start(Stage primaryStage) {
//        try {
//            logger.setFilter(new PingFilter());
//            logger.info("Starting client application...");
//
//            // Initialize SceneManager with primary stage
//            sceneManager = new SceneManager(primaryStage);
//
//            // Configure and show the stage
//            primaryStage.setTitle("Arcane Conquest");
//            primaryStage.setMinWidth(1000);
//            primaryStage.setMinHeight(700);
//
//            // Load the initial login scene
//            sceneManager.showLoginScreen();
//
//            primaryStage.show();
//
//        } catch (Exception e) {
//            logger.severe("Failed to start client application: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void stop() {
//        logger.info("Shutting down client application...");
//        // Clean up resources and disconnect if needed
//    }
//}