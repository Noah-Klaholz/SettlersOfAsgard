package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.client.core.PlayerIdentityManager;
import ch.unibas.dmi.dbis.cs108.client.core.state.GameState;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.StatueParameterRequirement;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.StatueParameterRequirement.StatueParameterType;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * Manages highlighting eligible tiles for statue placement and parameter
 * selection.
 * Can be used for both placing statues and selecting targets for statue
 * abilities.
 */
public class TileHighlightManager {
    private static final Logger LOGGER = Logger.getLogger(TileHighlightManager.class.getName());
    private final Canvas overlayCanvas;
    private final GameState gameState;
    private final BiConsumer<Integer, Integer> tileClickHandler; // Row, Column
    // Fields for state and callbacks
    private Mode currentMode = Mode.INACTIVE;
    private Statue activeStatue; // Used when in PARAMETER_SELECTION mode
    private StatueParameterRequirement activeRequirement; // Used when in PARAMETER_SELECTION mode
    private Color highlightColor = Color.YELLOW; // Default highlight color
    private Predicate<Tile> eligibilityCheck; // Custom logic for tile eligibility
    // For drawing
    private double effectiveHexSize;
    private double gridOffsetX;
    private double gridOffsetY;
    private double hSpacing;
    private double vSpacing;
    private double hSquish; // Horizontal squish factor for hexes
    private double vSquish; // Vertical squish factor for hexes
    private double rotationDegrees; // Rotation of hexes
    /**
     * Creates a TileHighlightManager.
     *
     * @param overlayCanvas    Canvas to draw highlights on
     * @param gameState        Current game state
     * @param tileClickHandler Callback for when a highlighted tile is clicked
     */
    public TileHighlightManager(Canvas overlayCanvas, GameState gameState,
                                BiConsumer<Integer, Integer> tileClickHandler) {
        this.overlayCanvas = overlayCanvas;
        this.gameState = gameState;
        this.tileClickHandler = tileClickHandler;
    }

    /**
     * Updates the geometry values used for drawing hex highlights.
     * Must be called whenever the board's geometry changes.
     *
     * @param effectiveHexSize Base size of hexes
     * @param gridOffsetX      X offset of grid
     * @param gridOffsetY      Y offset of grid
     * @param hSpacing         Horizontal spacing between hexes
     * @param vSpacing         Vertical spacing between hexes
     * @param hSquish          Horizontal squish factor
     * @param vSquish          Vertical squish factor
     * @param rotationDegrees  Rotation of hexes in degrees
     */
    public void updateGeometry(double effectiveHexSize, double gridOffsetX, double gridOffsetY,
                               double hSpacing, double vSpacing, double hSquish, double vSquish,
                               double rotationDegrees) {
        this.effectiveHexSize = effectiveHexSize;
        this.gridOffsetX = gridOffsetX;
        this.gridOffsetY = gridOffsetY;
        this.hSpacing = hSpacing;
        this.vSpacing = vSpacing;
        this.hSquish = hSquish;
        this.vSquish = vSquish;
        this.rotationDegrees = rotationDegrees;
    }

    /**
     * Activates statue placement mode, highlighting eligible tiles.
     *
     * @param statueId ID of the statue being placed
     */
    public void activateStatuePlacementMode(int statueId) {
        currentMode = Mode.STATUE_PLACEMENT;
        highlightColor = Color.GREEN;

        // Set eligibility check for statue placement
        // For Freyr (ID 31), only allow placement on Alfheim tiles
        if (statueId == 31) { // Freyr
            eligibilityCheck = tile -> "Alfheim".equals(tile.getWorld()) &&
                    tile.getOwner() != null &&
                    tile.getOwner().equals(PlayerIdentityManager.getInstance().getLocalPlayer()) &&
                    !tile.hasEntity();
        } else {
            // Default eligibility: owned, empty tiles
            eligibilityCheck = tile -> tile.getOwner() != null &&
                    tile.getOwner().equals(PlayerIdentityManager.getInstance().getLocalPlayer()) &&
                    !tile.hasEntity();
        }

        // Force redraw of highlights
        drawHighlights();
    }

    /**
     * Activates parameter selection mode for a statue ability.
     * For example, selecting a river tile for Freyr's "Grow Tree" ability.
     *
     * @param statue      The statue whose ability is being used
     * @param requirement The parameter requirement for the ability
     */
    public void activateParameterSelectionMode(Statue statue, StatueParameterRequirement requirement) {
        // Store statue and requirement for when a tile is clicked
        this.activeStatue = statue;
        this.activeRequirement = requirement;
        currentMode = Mode.PARAMETER_SELECTION;

        // Set highlight color based on parameter type
        highlightColor = Color.AQUA; // Default for parameters

        // Determine eligibility based on statue and parameter type
        if (statue.getId() == 31 && requirement.requires(StatueParameterType.TILE)) {
            // Freyr's "Grow Tree" needs a river tile
            eligibilityCheck = tile -> tile.hasRiver() && !tile.hasEntity();
            LOGGER.info("Activated parameter selection for Freyr's Grow Tree ability");
        } else if (requirement.requires(StatueParameterType.TILE)) {
            // Generic tile selection - owned tiles
            eligibilityCheck = tile -> tile.getOwner() != null &&
                    tile.getOwner().equals(PlayerIdentityManager.getInstance().getLocalPlayer());
        } else {
            // No highlighting if the requirement doesn't involve tiles
            clearHighlights();
            currentMode = Mode.INACTIVE;
            return;
        }

        // Force redraw of highlights
        drawHighlights();
    }

    /**
     * Handles a tile click based on the current mode.
     *
     * @param row Row of clicked tile
     * @param col Column of clicked tile
     * @return true if the click was handled, false if it should be passed to other
     * handlers
     */
    public boolean handleTileClick(int row, int col) {
        if (currentMode == Mode.INACTIVE) {
            return false;
        }

        Tile clickedTile = gameState.getBoardManager().getTile(col, row);
        if (clickedTile == null) {
            return false;
        }

        // Check if the tile is eligible based on current eligibility check
        if (eligibilityCheck != null && !eligibilityCheck.test(clickedTile)) {
            LOGGER.info("Clicked tile is not eligible for current action");
            return false;
        }

        if (currentMode == Mode.STATUE_PLACEMENT) {
            // Forward to the placement handler
            tileClickHandler.accept(row, col);

            // Deactivate highlighting mode
            deactivate();
            return true;
        }

        if (currentMode == Mode.PARAMETER_SELECTION && activeStatue != null && activeRequirement != null) {
            // Handle parameter selection based on statue and requirement
            if (activeRequirement.requires(StatueParameterType.TILE)) {
                // Call the handler with the tile that will create the UseStatueUIEvent
                tileClickHandler.accept(row, col);

                // Log and deactivate
                LOGGER.info(
                        "Parameter selected: Tile at (" + col + "," + row + ") for statue " + activeStatue.getName());
                deactivate();
                return true;
            }
        }

        return false;
    }

    /**
     * Deactivates highlighting and clears any highlights.
     */
    public void deactivate() {
        currentMode = Mode.INACTIVE;
        activeStatue = null;
        activeRequirement = null;
        clearHighlights();
    }

    /**
     * Clears all highlights from the overlay canvas.
     */
    public void clearHighlights() {
        if (overlayCanvas != null) {
            GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
        }
    }

    /**
     * Draws highlights for all eligible tiles based on current mode.
     */
    public void drawHighlights() {
        if (currentMode == Mode.INACTIVE || overlayCanvas == null) {
            return;
        }

        clearHighlights();
        GraphicsContext gc = overlayCanvas.getGraphicsContext2D();

        // Get number of rows and columns from the board
        int rows = gameState.getBoardManager().getBoard().getTiles().length;
        int cols = gameState.getBoardManager().getBoard().getTiles()[0].length;

        // Draw highlight for each eligible tile
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Tile tile = gameState.getBoardManager().getTile(col, row);
                if (tile != null && eligibilityCheck != null && eligibilityCheck.test(tile)) {
                    drawTileHighlight(gc, row, col);
                }
            }
        }
    }

    /**
     * Draws a highlight for a single tile.
     *
     * @param gc  GraphicsContext to draw on
     * @param row Row of the tile
     * @param col Column of the tile
     */
    private void drawTileHighlight(GraphicsContext gc, int row, int col) {
        // Calculate center of the hex
        double cx = gridOffsetX + col * hSpacing + (row % 2) * (hSpacing / 2);
        double cy = gridOffsetY + row * vSpacing;

        // Calculate hex vertices
        double rot = Math.toRadians(rotationDegrees);
        double[] xs = new double[6];
        double[] ys = new double[6];
        for (int i = 0; i < 6; i++) {
            double a = rot + 2 * Math.PI / 6 * i;
            xs[i] = cx + effectiveHexSize * Math.cos(a) * hSquish;
            ys[i] = cy + effectiveHexSize * Math.sin(a) * vSquish;
        }

        // Draw the highlight
        gc.setStroke(highlightColor);
        gc.setLineWidth(3);
        gc.setGlobalAlpha(0.8);
        gc.strokePolygon(xs, ys, 6);

        // Fill with semi-transparent color
        gc.setFill(highlightColor);
        gc.setGlobalAlpha(0.3);
        gc.fillPolygon(xs, ys, 6);
        gc.setGlobalAlpha(1.0);
    }

    /**
     * @return true if the manager is in any active highlighting mode
     */
    public boolean isActive() {
        return currentMode != Mode.INACTIVE;
    }

    /**
     * @return true if manager is in statue placement mode
     */
    public boolean isInStatuePlacementMode() {
        return currentMode == Mode.STATUE_PLACEMENT;
    }

    /**
     * @return true if manager is in parameter selection mode
     */
    public boolean isInParameterSelectionMode() {
        return currentMode == Mode.PARAMETER_SELECTION;
    }

    // Mode of operation
    private enum Mode {
        INACTIVE, // Not highlighting any tiles
        STATUE_PLACEMENT, // Highlighting tiles for statue placement
        PARAMETER_SELECTION // Highlighting tiles for parameter selection (e.g., target for Freyr's "Grow
        // Tree")
    }
}
