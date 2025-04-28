package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class ResourceOverviewPopup extends Popup {
    private static final Logger LOGGER = Logger.getLogger(ResourceOverviewPopup.class.getName());
    private final VBox container;
    private final ResourceLoader resourceLoader;
    private final Map<String, Color> playerColors;

    public ResourceOverviewPopup(ResourceLoader resourceLoader, Map<String, Color> playerColors) {
        this.resourceLoader = resourceLoader;
        this.playerColors = playerColors;

        // Main container
        container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setMaxWidth(500);
        container.setMaxHeight(600);

        // Load CSS
        container.getStylesheets().addAll(
                Objects.requireNonNull(getClass().getResource("/css/variables.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/css/common.css")).toExternalForm()
        );


        // Title
        Label title = new Label("Player Resources");
        title.getStyleClass().add("popup-title");
        container.getChildren().add(title);

        // Add scroll container for players
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("scroll-bar:vertical");

        // Player list container
        VBox playerList = new VBox(10);
        playerList.setPadding(new Insets(5));
        scrollPane.setContent(playerList);

        // Add a close button
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().addAll("standard-button", "primary-button");
        closeButton.setOnAction(e -> this.hide());

        container.getChildren().addAll(scrollPane, closeButton);
        this.getContent().add(container);
        this.setAutoHide(true);
    }

    public void updatePlayers(List<Player> players, String currentTurnPlayer) {
        // Clear existing player rows
        ScrollPane scrollPane = (ScrollPane) container.getChildren().get(1);
        VBox playerList = (VBox) scrollPane.getContent();
        playerList.getChildren().clear();

        // Create a row for each player
        for (Player player : players) {
            HBox playerRow = createPlayerResourceRow(player, player.getName().equals(currentTurnPlayer));
            playerList.getChildren().add(playerRow);
        }
    }

    private HBox createPlayerResourceRow(Player player, boolean isCurrentTurn) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setPrefWidth(450);

        // Set styling
        row.getStyleClass().add(isCurrentTurn ? "player-row-current" : "player-row");

        // Player color indicator and name
        Color playerColor = playerColors.getOrDefault(player.getName(), Color.GRAY);
        Circle colorIndicator = new Circle(8);
        colorIndicator.setFill(playerColor);
        colorIndicator.getStyleClass().add("player-color-indicator");

        Label nameLabel = new Label(player.getName());
        nameLabel.getStyleClass().add("player-name");
        nameLabel.setMinWidth(100);

        // Resources
        VBox resourcesBox = new VBox(5);
        resourcesBox.getStyleClass().add("resources-container");

        HBox runesBox = new HBox(5);
        runesBox.setAlignment(Pos.CENTER_LEFT);
        Label runesLabel = new Label("Runes: " + player.getRunes());
        runesLabel.getStyleClass().add("resource-label");
        runesBox.getChildren().add(runesLabel);

        HBox energyBox = new HBox(5);
        energyBox.setAlignment(Pos.CENTER_LEFT);
        Label energyLabel = new Label("Energy: " + player.getEnergy() + "/4");
        energyLabel.getStyleClass().add("resource-label");
        energyBox.getChildren().add(energyLabel);

        // Add tile count
        HBox tilesBox = new HBox(5);
        tilesBox.setAlignment(Pos.CENTER_LEFT);
        Label tilesLabel = new Label("Tiles: " + player.getOwnedTiles().size());
        tilesLabel.getStyleClass().add("resource-label");
        tilesBox.getChildren().add(tilesLabel);

        resourcesBox.getChildren().addAll(runesBox, energyBox, tilesBox);

        // Status effects
        VBox statusEffectsBox = new VBox(5);
        statusEffectsBox.setMinWidth(200);
        statusEffectsBox.getStyleClass().add("status-effects-container");

        Label statusLabel = new Label("Status Effects:");
        statusLabel.getStyleClass().add("status-header");
        statusEffectsBox.getChildren().add(statusLabel);

        Status status = player.getStatus();
        boolean hasEffects = false;

        // Add each buff type with its value
        for (Status.BuffType buffType : Status.BuffType.values()) {
            double value = status.get(buffType);

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

        // Add all to row
        row.getChildren().addAll(colorIndicator, nameLabel, resourcesBox, statusEffectsBox);

        return row;
    }

    private String formatBuffEffect(Status.BuffType buffType, double value) {
        String buffName = formatBuffName(buffType);
        String valueStr = (value > 1.0) ? "+" + Math.round((value - 1.0) * 100) + "%" :
                (value < 1.0) ? Math.round((1.0 - value) * 100) + "% reduction" :
                        "neutral";

        String description = getBuffDescription(buffType);
        return buffName + ": " + valueStr + "\n   " + description;
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

    private String getBuffDescription(Status.BuffType buffType) {
        // Provide short descriptions for each buff type
        return switch (buffType) {
            case SHOP_PRICE -> "Affects prices when buying items";
            case RUNE_GENERATION -> "Modifies rune generation";
            case RIVER_RUNE_GENERATION -> "Modifies rune generation from river tiles";
            case ARTIFACT_CHANCE -> "Modifies chance to find artifacts";
            case ENERGY_GENERATION -> "Modifies energy generation";
            case DEBUFFABLE -> "Determines if player can be debuffed";
            default -> "Affects gameplay mechanics";
        };
    }
}