package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * Handles highlighting eligible tiles for statue placement or special actions
 * like Freyr's tree growing on river tiles.
 */
public class StatueTileHighlighter {
    private static final Logger LOGGER = Logger.getLogger(StatueTileHighlighter.class.getName());

    private final Map<String, Node> highlightedTiles;
    private final Group highlightLayer;

    private BiConsumer<Integer, Integer> onTileSelectedCallback;

    /**
     * Creates a new tile highlighter.
     */
    public StatueTileHighlighter() {
        highlightedTiles = new HashMap<>();
        highlightLayer = new Group();
    }

    /**
     * Gets the layer containing all highlight elements.
     *
     * @return The highlight layer Group
     */
    public Group getHighlightLayer() {
        return highlightLayer;
    }

    /**
     * Highlights a list of tiles with a specific color.
     *
     * @param tiles          The tiles to highlight
     * @param onTileSelected Callback when a tile is selected
     */
    public void highlightTiles(List<Tile> tiles, BiConsumer<Integer, Integer> onTileSelected) {
        clearHighlights();

        onTileSelectedCallback = onTileSelected;

        for (Tile tile : tiles) {
            highlightTile(tile.getX(), tile.getY(), Color.LIGHTGREEN, false);
        }
    }

    /**
     * Highlights eligible river tiles for Freyr's tree growing.
     *
     * @param riverTiles     The list of river tiles
     * @param onTileSelected Callback when a river tile is selected
     */
    public void highlightRiverTiles(List<Tile> riverTiles, BiConsumer<Integer, Integer> onTileSelected) {
        clearHighlights();

        onTileSelectedCallback = onTileSelected;

        for (Tile tile : riverTiles) {
            // Only highlight river tiles that don't already have entities
            if (tile.hasRiver() && !tile.hasEntity()) {
                highlightTile(tile.getX(), tile.getY(), Color.LIGHTBLUE, false);
            }
        }
    }

    /**
     * Highlights a single tile with a specified color.
     *
     * @param x          The x coordinate
     * @param y          The y coordinate
     * @param color      The highlight color
     * @param isSelected Whether the tile is currently selected
     */
    public void highlightTile(int x, int y, Color color, boolean isSelected) {
        String key = x + "," + y;

        // Remove existing highlight if any
        if (highlightedTiles.containsKey(key)) {
            highlightLayer.getChildren().remove(highlightedTiles.get(key));
        }

        // Create highlight effect (placeholder - actual implementation would use proper
        // hex rendering)
        Rectangle highlight = new Rectangle(60, 60);
        highlight.setFill(color.deriveColor(0, 1, 1, 0.3));
        highlight.setStroke(color);
        highlight.setStrokeWidth(isSelected ? 3 : 1);

        // Add glow effect if selected
        if (isSelected) {
            Glow glow = new Glow(0.5);
            highlight.setEffect(glow);
        }

        // Position the highlight (this is simplified - would need proper positioning)
        // In a real implementation, this would calculate proper hex positions based on
        // coordinates

        // Add click handler
        highlight.setOnMouseClicked(e -> {
            if (onTileSelectedCallback != null) {
                onTileSelectedCallback.accept(x, y);
            }
        });

        // Add to layer and store reference
        highlightLayer.getChildren().add(highlight);
        highlightedTiles.put(key, highlight);
    }

    /**
     * Clears all highlighted tiles.
     */
    public void clearHighlights() {
        highlightLayer.getChildren().clear();
        highlightedTiles.clear();
        onTileSelectedCallback = null;
    }

    /**
     * Updates the highlight position when the grid is adjusted.
     * This would be called when the grid dimensions or positions change.
     */
    public void updateHighlightPositions() {
        // This would recalculate positions of all highlights based on current grid
        // dimensions
        // Implementation depends on how grid positioning is handled
    }
}
