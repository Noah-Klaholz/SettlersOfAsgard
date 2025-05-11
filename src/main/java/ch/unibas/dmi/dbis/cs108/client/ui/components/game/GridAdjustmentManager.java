package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.client.ui.controllers.GameScreenController;
import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Manages the state and interactions for adjusting the hexagonal grid overlay
 * in the GameScreen.
 */
public class GridAdjustmentManager {
    /**
     * Logger for GridAdjustmentManager.
     */
    private static final Logger LOGGER = Logger.getLogger(GridAdjustmentManager.class.getName());

    // Default grid parameters moved here
    /**
     * Default values for grid parameters.
     */
    private static final double DEF_GRID_SCALE = 0.85;
    /**
     * Default horizontal offset for the grid.
     */
    private static final double DEF_GRID_H_OFFSET = 0.00;
    /**
     * Default vertical offset for the grid.
     */
    private static final double DEF_GRID_V_OFFSET = 0.15;
    /**
     * Default width percentage for the grid.
     */
    private static final double DEF_GRID_WIDTH_PCT = 0.90;
    /**
     * Default height percentage for the grid.
     */
    private static final double DEF_GRID_HEIGHT_PCT = 2.06;
    /**
     * Default rotation degree for the grid.
     */
    private static final double DEF_ROTATION_DEG = 30.0;
    /**
     * Default horizontal spacing for the grid.
     */
    private static final double DEF_H_SPACING = 1.80;
    /**
     * Default vertical spacing for the grid.
     */
    private static final double DEF_V_SPACING = 1.33;
    /**
     * Default horizontal squish factor for the grid.
     */
    private static final double DEF_H_SQUISH = 1.00;
    /**
     * Default vertical squish factor for the grid.
     */
    private static final double DEF_V_SQUISH = 0.80;
    // References to UI elements and controller
    /**
     * The parent controller for the game screen.
     */
    private final GameScreenController gameScreenController;
    /**
     * The label indicating that adjustment mode is active.
     */
    private final Label adjustmentModeIndicator;
    /**
     * The label displaying the current adjustment values.
     */
    private final Label adjustmentValuesLabel;
    /**
     * A callback to trigger a redraw of the game screen.
     */
    private final Runnable redrawCallback;
    // Grid parameter fields moved here
    /**
     * The scale factor for the grid.
     */
    private double gridScaleFactor = DEF_GRID_SCALE;
    /**
     * The horizontal offset for the grid.
     */
    private double gridHorizontalOffset = DEF_GRID_H_OFFSET;
    /**
     * The vertical offset for the grid.
     */
    private double gridVerticalOffset = DEF_GRID_V_OFFSET;
    /**
     * The width percentage for the grid.
     */
    private double gridWidthPercentage = DEF_GRID_WIDTH_PCT;
    /**
     * The height percentage for the grid.
     */
    private double gridHeightPercentage = DEF_GRID_HEIGHT_PCT;
    /**
     * The rotation degree for the grid.
     */
    private double hexRotationDegrees = DEF_ROTATION_DEG;
    /**
     * The horizontal spacing factor for the grid.
     */
    private double horizontalSpacingFactor = DEF_H_SPACING;
    /**
     * The vertical spacing factor for the grid.
     */
    private double verticalSpacingFactor = DEF_V_SPACING;
    /**
     * The horizontal squish factor for the grid.
     */
    private double horizontalSquishFactor = DEF_H_SQUISH;
    /**
     * The vertical squish factor for the grid.
     */
    private double verticalSquishFactor = DEF_V_SQUISH;
    /**
     * Flag indicating whether grid adjustment mode is active.
     */
    private boolean gridAdjustmentModeActive = false;

    /**
     * Constructs a GridAdjustmentManager.
     *
     * @param gameScreenController    The parent controller.
     * @param adjustmentModeIndicator The label indicating adjustment mode is
     *                                active.
     * @param adjustmentValuesLabel   The label displaying current adjustment
     *                                values.
     * @param redrawCallback          A Runnable to trigger a redraw of the game
     *                                screen.
     */
    public GridAdjustmentManager(GameScreenController gameScreenController,
                                 Label adjustmentModeIndicator,
                                 Label adjustmentValuesLabel,
                                 Runnable redrawCallback) {
        this.gameScreenController = Objects.requireNonNull(gameScreenController, "GameScreenController cannot be null");
        this.adjustmentModeIndicator = Objects.requireNonNull(adjustmentModeIndicator,
                "AdjustmentModeIndicator label cannot be null");
        this.adjustmentValuesLabel = Objects.requireNonNull(adjustmentValuesLabel,
                "AdjustmentValuesLabel label cannot be null");
        this.redrawCallback = Objects.requireNonNull(redrawCallback, "Redraw callback cannot be null");
    }

    /**
     * Toggles grid-adjustment mode on/off.
     */
    public void toggleGridAdjustmentMode() {
        setGridAdjustmentMode(!gridAdjustmentModeActive);
    }

    /**
     * Enables or disables grid-adjustment mode.
     *
     * @param active {@code true} to enter adjustment mode, {@code false} to leave
     *               it.
     */
    public void setGridAdjustmentMode(boolean active) {
        gridAdjustmentModeActive = active;
        adjustmentModeIndicator.setVisible(active);
        adjustmentValuesLabel.setVisible(active);
        if (active) {
            gameScreenController.getGameCanvas().requestFocus(); // Request focus on the canvas
            updateAdjustmentFeedback();
        }
        redrawCallback.run(); // Redraw to show/hide grid lines or apply changes
    }

    /**
     * Handles keyboard shortcuts related to grid adjustment mode.
     *
     * @param e The KeyEvent.
     * @return true if the event was consumed, false otherwise.
     */
    public boolean handleKeyboardShortcut(KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE && gridAdjustmentModeActive) {
            setGridAdjustmentMode(false);
            return true;
        }
        if (e.isControlDown() && e.getCode() == KeyCode.G) {
            toggleGridAdjustmentMode();
            return true;
        }
        return false;
    }

    /**
     * Processes key presses while in grid-adjustment mode.
     *
     * @param e The KeyEvent.
     */
    public void handleGridAdjustmentKeys(KeyEvent e) {
        if (!gridAdjustmentModeActive) {
            // Allow Ctrl+G even if not active to enter the mode
            if (e.isControlDown() && e.getCode() == KeyCode.G) {
                toggleGridAdjustmentMode();
                e.consume();
            }
            return;
        }

        boolean changed = true; // Assume a change happened unless default case hit
        switch (e.getCode()) {
            case UP -> gridVerticalOffset -= 0.01;
            case DOWN -> gridVerticalOffset += 0.01;
            case LEFT -> gridHorizontalOffset -= 0.01;
            case RIGHT -> gridHorizontalOffset += 0.01;
            case PLUS, ADD -> gridScaleFactor = Math.min(1.5, gridScaleFactor + 0.05);
            case MINUS, SUBTRACT -> gridScaleFactor = Math.max(0.3, gridScaleFactor - 0.05);
            case W -> gridHeightPercentage -= 0.02;
            case S -> gridHeightPercentage += 0.02;
            case A -> gridWidthPercentage -= 0.02;
            case D -> gridWidthPercentage += 0.02;
            case R -> hexRotationDegrees = (hexRotationDegrees + 10) % 60;
            case T -> hexRotationDegrees = (hexRotationDegrees - 10 + 60) % 60;
            case F -> horizontalSpacingFactor = Math.max(1, horizontalSpacingFactor - 0.1);
            case G -> horizontalSpacingFactor += 0.1;
            case V -> verticalSpacingFactor = Math.max(1, verticalSpacingFactor - 0.1);
            case B -> verticalSpacingFactor += 0.1;
            case H -> horizontalSquishFactor = Math.max(0.5, horizontalSquishFactor - 0.1);
            case J -> horizontalSquishFactor = Math.min(2, horizontalSquishFactor + 0.1);
            case K -> verticalSquishFactor = Math.max(0.5, verticalSquishFactor - 0.1);
            case L -> verticalSquishFactor = Math.min(2, verticalSquishFactor + 0.1);
            case BACK_SPACE -> resetDefaults();
            case P -> LOGGER.info(getGridSettings());
            case C -> copyGridSettingsToClipboard();
            case ESCAPE -> setGridAdjustmentMode(false); // Allow Esc to exit mode
            default -> changed = false; // No relevant key pressed
        }

        if (changed) {
            redrawCallback.run();
            updateAdjustmentFeedback();
            e.consume();
        }
    }

    /**
     * Restores all grid parameters to their factory defaults.
     */
    private void resetDefaults() {
        gridScaleFactor = DEF_GRID_SCALE;
        gridHorizontalOffset = DEF_GRID_H_OFFSET;
        gridVerticalOffset = DEF_GRID_V_OFFSET;
        gridWidthPercentage = DEF_GRID_WIDTH_PCT;
        gridHeightPercentage = DEF_GRID_HEIGHT_PCT;
        hexRotationDegrees = DEF_ROTATION_DEG;
        horizontalSpacingFactor = DEF_H_SPACING;
        verticalSpacingFactor = DEF_V_SPACING;
        horizontalSquishFactor = DEF_H_SQUISH;
        verticalSquishFactor = DEF_V_SQUISH;
        LOGGER.info("Grid settings reset to defaults.");
    }

    /**
     * Presents the current parameter values while adjusting.
     */
    private void updateAdjustmentFeedback() {
        adjustmentValuesLabel.setText(String.format(
                "Scale: %.2f | Offset:(%.2f,%.2f) | Size: %.0f%%x%.0f%% | Rot: %.1f° | " +
                        "Spacing:(%.2f,%.2f) | Squish:(%.2f,%.2f)",
                gridScaleFactor, gridHorizontalOffset, gridVerticalOffset,
                gridWidthPercentage * 100, gridHeightPercentage * 100, hexRotationDegrees,
                horizontalSpacingFactor, verticalSpacingFactor, horizontalSquishFactor, verticalSquishFactor));
    }

    /**
     * Copies the grid parameter string to the system clipboard and flashes a green
     * background as feedback.
     */
    private void copyGridSettingsToClipboard() {
        ClipboardContent content = new ClipboardContent();
        String settings = getGridSettings();
        content.putString(settings);
        Clipboard.getSystemClipboard().setContent(content);
        LOGGER.info("Copied to clipboard: " + settings);

        // Flash feedback
        String originalStyle = adjustmentValuesLabel.getStyle();
        adjustmentValuesLabel.setStyle(originalStyle + "-fx-background-color:rgba(0,128,0,0.8);");
        PauseTransition flash = new PauseTransition(Duration.seconds(0.5));
        flash.setOnFinished(ev -> adjustmentValuesLabel.setStyle(originalStyle)); // Restore original style
        flash.play();
    }

    /**
     * Returns a formatted string with all grid parameters.
     *
     * @return A string representation of the grid settings.
     */
    public String getGridSettings() {
        return String.format(
                "Grid settings: scale=%.2f, hOffset=%.2f, vOffset=%.2f, width=%.2f%%, height=%.2f%%, rotation=%.1f°, " +
                        "hSpacing=%.2f, vSpacing=%.2f, hSquish=%.2f, vSquish=%.2f",
                gridScaleFactor, gridHorizontalOffset, gridVerticalOffset, gridWidthPercentage * 100,
                gridHeightPercentage * 100, hexRotationDegrees, horizontalSpacingFactor, verticalSpacingFactor,
                horizontalSquishFactor, verticalSquishFactor);
    }

    // --- Getters for grid parameters needed by GameScreenController ---

    /**
     * Getters for grid parameters to be used by GameScreenController.
     * These methods provide access to the current grid settings.
     */
    /**
     * Returns the current grid scale factor.
     *
     * @return The grid scale factor.
     */
    public double getGridScaleFactor() {
        return gridScaleFactor;
    }


    /**
     * Returns the current grid horizontal offset.
     *
     * @return The grid horizontal offset.
     */
    public double getGridHorizontalOffset() {
        return gridHorizontalOffset;
    }

    /**
     * Returns the current grid vertical offset.
     *
     * @return The grid vertical offset.
     */
    public double getGridVerticalOffset() {
        return gridVerticalOffset;
    }

    /**
     * Returns the current grid width percentage.
     *
     * @return The grid width percentage.
     */
    public double getGridWidthPercentage() {
        return gridWidthPercentage;
    }

    /**
     * Returns the current grid height percentage.
     *
     * @return The grid height percentage.
     */
    public double getGridHeightPercentage() {
        return gridHeightPercentage;
    }

    /**
     * Returns the current hexagon rotation degrees.
     *
     * @return The hexagon rotation degrees.
     */
    public double getHexRotationDegrees() {
        return hexRotationDegrees;
    }

    /**
     * Returns the current horizontal spacing factor.
     *
     * @return The horizontal spacing factor.
     */
    public double getHorizontalSpacingFactor() {
        return horizontalSpacingFactor;
    }

    /**
     * Returns the current vertical spacing factor.
     *
     * @return The vertical spacing factor.
     */
    public double getVerticalSpacingFactor() {
        return verticalSpacingFactor;
    }

    /**
     * Returns the current horizontal squish factor.
     *
     * @return The horizontal squish factor.
     */
    public double getHorizontalSquishFactor() {
        return horizontalSquishFactor;
    }

    /**
     * Returns the current vertical squish factor.
     *
     * @return The vertical squish factor.
     */
    public double getVerticalSquishFactor() {
        return verticalSquishFactor;
    }

    /**
     * Returns whether grid adjustment mode is currently active.
     *
     * @return {@code true} if grid adjustment mode is active, {@code false}
     *         otherwise.
     */
    public boolean isGridAdjustmentModeActive() {
        return gridAdjustmentModeActive;
    }
}
