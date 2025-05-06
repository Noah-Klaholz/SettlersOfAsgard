package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * A tooltip for displaying tile information.
 * Uses the same pattern as the card tooltips.
 */
public class TileTooltip {
    private final Tooltip tooltip;

    public TileTooltip(Tile tile) {
        tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setHideDelay(Duration.ZERO); // Ensure it disappears immediately when mouse leaves
        
        // Create a layout with styled sections
        VBox content = new VBox(5);
        content.setPadding(new Insets(8));
        content.setMaxWidth(250);
        content.getStyleClass().add("tooltip-content");
        
        // Title
        Label titleLabel = new Label("Tile Information");
        titleLabel.getStyleClass().add("tooltip-title");
        content.getChildren().add(titleLabel);
        content.getChildren().add(new Separator());
        
        // World info
        Label worldLabel = new Label("World: " + tile.getWorld());
        worldLabel.getStyleClass().add("tooltip-world");
        content.getChildren().add(worldLabel);
        
        // Resource value
        Label resourceLabel = new Label("Resource Value: " + tile.getResourceValue());
        resourceLabel.getStyleClass().add("tooltip-resource");
        content.getChildren().add(resourceLabel);
        
        // Ownership/Price info
        String ownerName = tile.getOwner();
        if (ownerName != null && !ownerName.isEmpty()) {
            Label ownerLabel = new Label("Owner: " + ownerName);
            ownerLabel.getStyleClass().add("tooltip-owner");
            content.getChildren().add(ownerLabel);
        } else {
            Label priceLabel = new Label("Price: " + tile.getPrice() + " runes");
            priceLabel.getStyleClass().add("tooltip-price");
            content.getChildren().add(priceLabel);
        }
        
        // Entity information if present
        if (tile.hasEntity()) {
            GameEntity entity = EntityRegistry.getGameEntityOriginalById(tile.getEntity().getId());
            
            content.getChildren().add(new Separator());
            
            Label entityLabel = new Label(entity.getName());
            entityLabel.getStyleClass().add("tooltip-entity");
            content.getChildren().add(entityLabel);
            
            if (entity.getUsage() != null && !entity.getUsage().isEmpty()) {
                Label usageLabel = new Label(entity.getUsage());
                usageLabel.getStyleClass().add("tooltip-usage");
                usageLabel.setWrapText(true);
                content.getChildren().add(usageLabel);
            }
            
            // Add level information for statues
            if (entity instanceof Statue s) {
                Label levelLabel = new Label("Level: " + s.getLevel());
                levelLabel.getStyleClass().add("tooltip-level");
                content.getChildren().add(levelLabel);
            }
        }
        
        // Set the tooltip properties
        tooltip.setMaxWidth(250);
        tooltip.setMaxHeight(300);
        content.setMinHeight(Region.USE_PREF_SIZE);
        content.setPrefHeight(Region.USE_COMPUTED_SIZE);
        content.setMaxHeight(Region.USE_PREF_SIZE);
        
        tooltip.setGraphic(content);
        tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        tooltip.getStyleClass().add("tile-tooltip");
    }

    /**
     * Closes all resources for the tooltip and hides it.
     */
    public void close() {
        tooltip.hide();
        tooltip.getGraphic().setVisible(false);
        tooltip.getGraphic().setManaged(false);
    }

    /**
     * Gets the tooltip object.
     * @return The JavaFX tooltip
     */
    public Tooltip getTooltip() {
        return tooltip;
    }
}
