package ch.unibas.dmi.dbis.cs108.client.ui.game;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class GameCanvas {
    private static final Logger logger = Logger.getLogger(GameCanvas.class.getName());
    // Game grid constants
    private static final int HEX_SIZE = 40;
    private static final int GRID_WIDTH = 15;
    private static final int GRID_HEIGHT = 15;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Map<String, Image> imageCache = new HashMap<>();

    public GameCanvas(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();

        // Make canvas resizable
        canvas.widthProperty().addListener((obs, oldVal, newVal) -> redrawCanvas());
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> redrawCanvas());

        // Initialize image cache
        loadImages();
    }

    private void loadImages() {
        try {
            // Load and cache common game images
            imageCache.put("background", new Image(getClass().getResourceAsStream("/images/background.png")));
            imageCache.put("hex_empty", new Image(getClass().getResourceAsStream("/images/hex_empty.png")));
            imageCache.put("hex_energy", new Image(getClass().getResourceAsStream("/images/hex_energy.png")));
            imageCache.put("hex_rune", new Image(getClass().getResourceAsStream("/images/hex_rune.png")));

            logger.info("Game images loaded successfully");
        } catch (Exception e) {
            logger.warning("Failed to load game images: " + e.getMessage());
        }
    }

    public void initializeMap() {
        redrawCanvas();
    }

    public void redrawCanvas() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Clear canvas
        gc.clearRect(0, 0, width, height);

        // Draw background
        Image background = imageCache.get("background");
        if (background != null) {
            gc.drawImage(background, 0, 0, width, height);
        } else {
            // Fallback background
            gc.setFill(Color.DARKGREEN);
            gc.fillRect(0, 0, width, height);
        }

        // Draw hexagonal grid
        drawHexagonalGrid();
    }

    private void drawHexagonalGrid() {
        double hexWidth = HEX_SIZE * 2;
        double hexHeight = Math.sqrt(3) * HEX_SIZE;
        double xOffset = 50; // Left margin
        double yOffset = 50; // Top margin

        Image hexImage = imageCache.get("hex_empty");

        for (int row = 0; row < GRID_HEIGHT; row++) {
            for (int col = 0; col < GRID_WIDTH; col++) {
                double x = xOffset + col * (hexWidth * 0.75);
                double y = yOffset + row * hexHeight + (col % 2 == 0 ? 0 : hexHeight / 2);

                if (hexImage != null) {
                    gc.drawImage(hexImage, x, y, hexWidth, hexHeight);
                } else {
                    // Fallback - draw hexagon shape
                    drawHexagon(x + hexWidth / 2, y + hexHeight / 2, HEX_SIZE);
                }
            }
        }
    }

    private void drawHexagon(double centerX, double centerY, double size) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);

        double[] xPoints = new double[6];
        double[] yPoints = new double[6];

        for (int i = 0; i < 6; i++) {
            xPoints[i] = centerX + size * Math.cos(i * Math.PI / 3);
            yPoints[i] = centerY + size * Math.sin(i * Math.PI / 3);
        }

        for (int i = 0; i < 6; i++) {
            int next = (i + 1) % 6;
            gc.strokeLine(xPoints[i], yPoints[i], xPoints[next], yPoints[next]);
        }
    }

    public void updateGameState(String gameState) {
        // Here we would parse the game state and update the canvas accordingly
        // For now we'll just redraw the canvas
        redrawCanvas();
    }
}