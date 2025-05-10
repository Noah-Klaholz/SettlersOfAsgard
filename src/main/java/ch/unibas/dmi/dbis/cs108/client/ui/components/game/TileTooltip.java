package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.StylesheetLoader;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.logging.Logger;

/**
 * A tooltip for displaying tile information.
 * Uses the same pattern as the card tooltips.
 */
public class TileTooltip {
    private static final Logger LOGGER = Logger.getLogger(TileTooltip.class.getName());
    private final Tooltip tooltip;

    public TileTooltip(Tile tile) {
        tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(500)); // Show immediately
        tooltip.setHideDelay(Duration.ZERO); // Ensure it disappears immediately when mouse leaves

        Scene scene = new Scene(new VBox());
        StylesheetLoader.loadStylesheet(scene.getRoot(), "/css/ressource-overview.css");


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

            VBox statusEffectsBox = new VBox(5);
            statusEffectsBox.setMinWidth(200);
            statusEffectsBox.getStyleClass().add("status-effects-container");

            Label statusLabel = new Label("Status Effects:");
            statusLabel.getStyleClass().add("status-header");
            statusEffectsBox.getChildren().add(statusLabel);

            Status status = tile.getStatus();
            if (status == null) {
                LOGGER.warning("Tile " + tile.getX() + tile.getY() + " has no status object!");
            } else {
                boolean hasEffects = false;

                // Add each buff type with its value
                for (Status.BuffType buffType : Status.BuffType.values()) {
                    double value = Math.round(status.get(buffType) * 100) / 100.0; // Round to 2 decimal places

                    // Only show non-default values
                    if (value != 1.0) {
                        hasEffects = true;
                        String effect = formatBuffEffect(buffType, value);
                        Label effectLabel = new Label(effect);

                        if (value > 1.0) {
                            effectLabel.getStyleClass().add("buff-positive");
                        } else if (value < 1.0) {
                            effectLabel.getStyleClass().add("buff-negative");
                        } else {
                            effectLabel.getStyleClass().add("buff-neutral");
                        }

                        statusEffectsBox.getChildren().add(effectLabel);
                    }
                }

                if (!hasEffects) {
                    Label noEffectsLabel = new Label("No active effects");
                    noEffectsLabel.getStyleClass().add("no-effects-label");
                    statusEffectsBox.getChildren().add(noEffectsLabel);
                }
            }
        } else {
            Label priceLabel = new Label("Price: " + tile.getPrice() + " runes");
            priceLabel.getStyleClass().add("tooltip-price");
            content.getChildren().add(priceLabel);
        }

        // Entity information if present
        if (tile.hasEntity()) {
            GameEntity entity = tile.getEntity();

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

    private String formatBuffEffect(Status.BuffType buffType, double value) {
        String buffName = formatBuffName(buffType);
        double roundedValue = Math.round((value - 1.0) * 100) / 100.0; // Round to 2 decimal places
        String valueStr = (value > 1.0) ? "+" + roundedValue * 100 + "%" :
                (value < 1.0) ? Math.abs(roundedValue * 100) + "% reduction" :
                        "neutral";

        return buffName + ": " + valueStr;
    }

    private String formatBuffName(Status.BuffType buffType) {
        // Convert enum name to readable format (e.g., SHOP_PRICE -> Shop Price)
        String name = buffType.toString();
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            result.append(word.charAt(0)).append(word.substring(1).toLowerCase()).append(" ");
        }

        return result.toString().trim();
    }

    /**
     * Closes all resources for the tooltip and hides it.
     */
    public void close() {
        Tooltip tooltip = getTooltip();
        if (tooltip != null) {
            tooltip.hide();

            // Uninstall the tooltip if it has an owner
            if (tooltip.getOwnerNode() != null) {
                Tooltip.uninstall(tooltip.getOwnerNode(), tooltip);
            }

            // Remove graphics
            if (tooltip.getGraphic() != null) {
                tooltip.getGraphic().setVisible(false);
                tooltip.getGraphic().setManaged(false);
            }
        }
    }

    /**
     * Gets the tooltip object.
     *
     * @return The JavaFX tooltip
     */
    public Tooltip getTooltip() {
        return tooltip;
    }
}
