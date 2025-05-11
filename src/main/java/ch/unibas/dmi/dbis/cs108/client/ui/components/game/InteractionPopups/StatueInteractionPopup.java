package ch.unibas.dmi.dbis.cs108.client.ui.components.game.InteractionPopups;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Popup for interacting with statues, providing level-dependent options
 */
public class StatueInteractionPopup extends TileInteractionPopup {
    /**
     * Logger for StatueInteractionPopup.
     */
    private static final Logger LOGGER = Logger.getLogger(StatueInteractionPopup.class.getName());
    /**
     * The statue being interacted with.
     */
    private final Statue statue;
    /**
     * Callbacks for level up, deal, and blessing actions.
     */
    private final Consumer<Tile> onLevelUp;
    /**
     * Callback for making a deal with the statue.
     */
    private final Consumer<Tile> onDeal;
    /**
     * Callback for receiving a blessing from the statue.
     */
    private final Consumer<Tile> onBlessing;

    /**
     * Constructor for StatueInteractionPopup.
     *
     * @param resourceLoader Resource loader for loading UI resources.
     * @param tile           The tile associated with the statue.
     * @param playerName     The name of the player interacting with the statue.
     * @param onLevelUp      Callback for level up action.
     * @param onDeal         Callback for making a deal with the statue.
     * @param onBlessing     Callback for receiving a blessing from the statue.
     */
    public StatueInteractionPopup(ResourceLoader resourceLoader, Tile tile, String playerName,
                                  Consumer<Tile> onLevelUp, Consumer<Tile> onDeal, Consumer<Tile> onBlessing) {
        super(resourceLoader, tile, playerName);
        this.statue = (Statue) entity;
        this.onLevelUp = onLevelUp;
        this.onDeal = onDeal;
        this.onBlessing = onBlessing;

        initializeUI();
    }

    /**
     * Initializes the UI components for the statue interaction popup.
     */
    private void initializeUI() {
        // Show statue level
        Label levelLabel = new Label("Current Level: " + statue.getLevel());
        levelLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -color-accent-gold;");
        container.getChildren().add(levelLabel);

        // Action buttons depend on the statue level
        VBox actionsBox = new VBox(10);
        actionsBox.setAlignment(Pos.CENTER);

        // Level up button (always available)
        Button levelUpButton = new Button("Level Up Statue");
        levelUpButton.getStyleClass().addAll("standard-button", "primary-button");
        levelUpButton.setOnAction(e -> {
            onLevelUp.accept(tile);
            this.hide();
        });

        actionsBox.getChildren().add(levelUpButton);

        // Level-dependent actions
        if (statue.getLevel() >= 2) {
            Button dealButton = new Button("Make a Deal");
            dealButton.getStyleClass().addAll("standard-button", "accent-button");
            dealButton.setOnAction(e -> {
                onDeal.accept(tile);
                this.hide();
            });
            actionsBox.getChildren().add(dealButton);

            Label dealInfoLabel = new Label("Statue offers: " + getDealDescription());
            dealInfoLabel.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-style: italic;");
            dealInfoLabel.setWrapText(true);
            actionsBox.getChildren().add(dealInfoLabel);
        }

        if (statue.getLevel() >= 3) {
            Button blessingButton = new Button("Receive Blessing");
            blessingButton.getStyleClass().addAll("standard-button", "accent-button");
            blessingButton.setOnAction(e -> {
                onBlessing.accept(tile);
                this.hide();
            });
            actionsBox.getChildren().add(blessingButton);

            Label blessingInfoLabel = new Label("Statue blessing: " + getBlessingDescription());
            blessingInfoLabel.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-style: italic;");
            blessingInfoLabel.setWrapText(true);
            actionsBox.getChildren().add(blessingInfoLabel);
        }

        container.getChildren().add(actionsBox);
        addCloseButton();
    }

    /**
     * Adds a close button to the popup.
     */
    private String getDealDescription() {
        // You can replace this with actual statue-specific deal descriptions
        return switch (statue.getId()) {
            case 30 -> "Choose an artifact to replace with a new one";
            case 31 -> "Choose a player to give energy to";
            case 32 -> "Choose a monument to place on your tile";
            case 33 -> "Choose a resource to gain";
            case 34 -> "Choose a direction to expand your territory";
            case 35 -> "Choose a buff to apply to yourself";
            case 36 -> "Choose a player to apply a debuff to";
            case 37 -> "Choose a tile to gain ownership of";
            default -> "Special deal based on statue type";
        };
    }

    /**
     * Adds a close button to the popup.
     */
    private String getBlessingDescription() {
        // You can replace this with actual statue-specific blessing descriptions
        return switch (statue.getId()) {
            case 30 -> "Gain two artifacts of your choice";
            case 31 -> "All your players gain energy";
            case 32 -> "Place two monuments on your tiles";
            case 33 -> "Gain resources from all your tiles";
            case 34 -> "Expand your territory in multiple directions";
            case 35 -> "Apply multiple buffs to yourself";
            case 36 -> "Apply debuffs to all other players";
            case 37 -> "Gain ownership of multiple tiles";
            default -> "Enhanced blessing based on statue type";
        };
    }
}