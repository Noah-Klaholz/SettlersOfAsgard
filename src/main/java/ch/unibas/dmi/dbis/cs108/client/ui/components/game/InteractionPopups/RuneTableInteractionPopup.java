package ch.unibas.dmi.dbis.cs108.client.ui.components.game.InteractionPopups;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Popup for interacting with rune table structures
 */
public class RuneTableInteractionPopup extends TileInteractionPopup {
    private static final Logger LOGGER = Logger.getLogger(RuneTableInteractionPopup.class.getName());
    private final Consumer<Tile> onUseRuneTable;

    public RuneTableInteractionPopup(ResourceLoader resourceLoader, Tile tile, String playerName,
                                     Consumer<Tile> onUseRuneTable) {
        super(resourceLoader, tile, playerName);
        this.onUseRuneTable = onUseRuneTable;

        initializeUI();
    }

    private void initializeUI() {
        // Simple "Use" button for rune table
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button useButton = new Button("Use Rune Table");
        useButton.getStyleClass().addAll("standard-button", "primary-button");
        useButton.setOnAction(e -> {
            onUseRuneTable.accept(tile);
            this.hide();
        });

        buttonBox.getChildren().add(useButton);
        container.getChildren().add(buttonBox);

        addCloseButton();
    }
}