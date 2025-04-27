package ch.unibas.dmi.dbis.cs108.client.ui.components.game.InteractionPopups;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.logging.Logger;

/**
 * Base class for popups that allow players to interact with structures/statues on tiles
 */
public abstract class TileInteractionPopup extends Popup {
    private static final Logger LOGGER = Logger.getLogger(TileInteractionPopup.class.getName());
    protected final ResourceLoader resourceLoader;
    protected final Tile tile;
    protected final GameEntity entity;
    protected final String playerName;
    protected VBox container;

    public TileInteractionPopup(ResourceLoader resourceLoader, Tile tile, String playerName) {
        this.resourceLoader = resourceLoader;
        this.tile = tile;
        this.playerName = playerName;
        this.entity = EntityRegistry.getGameEntityOriginalById(tile.getEntity().getId());

        initializeBaseUI();
    }

    private void initializeBaseUI() {
        container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setMaxWidth(350);

        // Apply CSS styles
        container.getStylesheets().addAll(
                getClass().getResource("/css/variables.css").toExternalForm(),
                getClass().getResource("/css/common.css").toExternalForm()
        );

        container.setStyle("-fx-background-color: -color-background-primary; " +
                "-fx-border-color: -color-accent-gold; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 5px;");

        // Header with entity name
        Label title = new Label(entity.getName());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        // Entity image and description
        HBox infoBox = createEntityInfoBox();

        container.getChildren().addAll(title, infoBox);

        // Subclasses will add specific interaction controls

        this.getContent().add(container);
        this.setAutoHide(true);
    }

    protected HBox createEntityInfoBox() {
        HBox infoBox = new HBox(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        // Entity image
        ImageView imageView = new ImageView();
        imageView.setFitHeight(80);
        imageView.setFitWidth(60);
        imageView.setPreserveRatio(true);

        String imageUrl = EntityRegistry.getURL(entity.getId(), true);
        Image image = resourceLoader.loadImage(imageUrl);

        if (image != null && !image.isError()) {
            imageView.setImage(image);
        }

        // Entity description
        VBox descBox = new VBox(5);
        HBox.setHgrow(descBox, Priority.ALWAYS);

        if (entity.getDescription() != null && !entity.getDescription().isEmpty()) {
            Label descLabel = new Label(entity.getDescription());
            descLabel.setStyle("-fx-text-fill: -color-text-secondary;");
            descLabel.setWrapText(true);
            descBox.getChildren().add(descLabel);
        }

        if (entity.getUsage() != null && !entity.getUsage().isEmpty()) {
            Label usageLabel = new Label(entity.getUsage());
            usageLabel.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-style: italic;");
            usageLabel.setWrapText(true);
            descBox.getChildren().add(usageLabel);
        }

        infoBox.getChildren().addAll(imageView, descBox);
        return infoBox;
    }

    /**
     * Shows this popup next to the provided node
     */
    public void showNear(Node source) {
        if (!this.isShowing()) {
            this.show(source.getScene().getWindow(),
                    source.localToScreen(source.getBoundsInLocal()).getCenterX() - 175,
                    source.localToScreen(source.getBoundsInLocal()).getCenterY() - 100);
        }
    }

    /**
     * Adds a close button to the container
     */
    protected void addCloseButton() {
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().addAll("standard-button", "secondary-button");
        closeButton.setOnAction(e -> this.hide());
        container.getChildren().add(closeButton);
    }
}