package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Custom tooltip that displays information about a tile and any entity on it.
 */
public class TileTooltip extends Tooltip {

    public TileTooltip(Tile tile, String ownerName) {
        this.setShowDelay(Duration.millis(300));
        this.setHideDelay(Duration.millis(200));

        VBox content = new VBox(5);
        content.setPadding(new Insets(8));
        content.setMaxWidth(250);
        content.getStyleClass().add("tooltip-content");

        // Tile information
        Label worldLabel = new Label("World: " + tile.getWorld());
        worldLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-text-primary;");
        content.getChildren().add(worldLabel);

        Label resourceLabel = new Label("Resource Value: " + tile.getResourceValue());
        resourceLabel.setStyle("-fx-text-fill: -color-text-secondary;");
        content.getChildren().add(resourceLabel);

        if (ownerName != null && !ownerName.isEmpty()) {
            Label ownerLabel = new Label("Owner: " + ownerName);
            ownerLabel.setStyle("-fx-text-fill: -color-text-secondary;");
            content.getChildren().add(ownerLabel);
        } else {
            Label priceLabel = new Label("Price: " + tile.getPrice() + " runes");
            priceLabel.setStyle("-fx-text-fill: -color-accent-gold;");
            content.getChildren().add(priceLabel);
        }

        // If the tile has an entity, add entity information
        if (tile.hasEntity()) {
            GameEntity entity = EntityRegistry.getGameEntityOriginalById(tile.getEntity().getId());

            Label separator = new Label("─".repeat(25));
            separator.setStyle("-fx-text-fill: -color-text-secondary;");
            content.getChildren().add(separator);

            Label entityLabel = new Label(entity.getName());
            entityLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-text-primary;");
            content.getChildren().add(entityLabel);

            if (entity.getUsage() != null && !entity.getUsage().isEmpty()) {
                Label usageLabel = new Label(entity.getUsage());
                usageLabel.setStyle("-fx-text-fill: -color-text-secondary;");
                usageLabel.setWrapText(true);
                content.getChildren().add(usageLabel);
            }

            // Add level information for statues
            if (entity instanceof Statue s) {
                Label levelLabel = new Label("Level: " + s.getLevel());
                levelLabel.setStyle("-fx-text-fill: -color-accent-positive;");
                content.getChildren().add(levelLabel);
            }
        }

        // Configure tooltip properties
        this.setMaxWidth(250);
        this.setMaxHeight(300);
        content.setMinHeight(Region.USE_PREF_SIZE);
        content.setPrefHeight(Region.USE_COMPUTED_SIZE);
        content.setMaxHeight(Region.USE_PREF_SIZE);

        this.setGraphic(content);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.getStyleClass().add("tile-tooltip");
    }
}