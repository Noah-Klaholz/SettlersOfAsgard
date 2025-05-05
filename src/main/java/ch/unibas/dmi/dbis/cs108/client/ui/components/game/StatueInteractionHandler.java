package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.client.ui.SceneManager;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.UpgradeStatueUIEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.UseStatueUIEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.StatueDetailsWrapper;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Handles interactions with statues, including showing context menus,
 * confirmation dialogs, and dispatching events for statue actions.
 */
public class StatueInteractionHandler {
    private static final Logger LOGGER = Logger.getLogger(StatueInteractionHandler.class.getName());

    private final UIEventBus eventBus;
    private final ResourceLoader resourceLoader;
    private final Parent rootNode;

    private StatueContextMenu contextMenu;
    private StatueConfirmationDialog confirmationDialog;
    private StatueSidePanel sidePanel;
    private StatueTileHighlighter tileHighlighter;

    private Player currentPlayer;
    private int currentRunes;
    private int currentEnergy;

    /**
     * Creates a new statue interaction handler.
     * 
     * @param eventBus       The UI event bus for dispatching events
     * @param resourceLoader The resource loader for loading resources
     * @param rootNode       The root node of the scene
     */
    public StatueInteractionHandler(UIEventBus eventBus, ResourceLoader resourceLoader, Parent rootNode) {
        this.eventBus = eventBus;
        this.resourceLoader = resourceLoader;
        this.rootNode = rootNode;

        initializeComponents();
    }

    /**
     * Initializes UI components used for statue interactions.
     */
    private void initializeComponents() {
        contextMenu = new StatueContextMenu();
        confirmationDialog = new StatueConfirmationDialog(resourceLoader);
        sidePanel = new StatueSidePanel();
        tileHighlighter = new StatueTileHighlighter();

        if (rootNode instanceof Pane paneRoot) {
            // Add side panel to the right side of the root node
            sidePanel.setVisible(false);
            paneRoot.getChildren().add(sidePanel);
        }

        // Set up action handlers
        contextMenu.setActionHandler(this::handleContextMenuAction);
        sidePanel.setActionHandler(this::handleSidePanelAction);
    }

    /**
     * Updates the player information used for interaction decisions.
     * 
     * @param player The current player
     * @param runes  The player's current runes
     * @param energy The player's current energy
     */
    public void updatePlayerInfo(Player player, int runes, int energy) {
        this.currentPlayer = player;
        this.currentRunes = runes;
        this.currentEnergy = energy;
    }

    /**
     * Shows the context menu for a statue.
     * 
     * @param statue The statue details
     * @param x      The x coordinate in screen coordinates
     * @param y      The y coordinate in screen coordinates
     * @param tile   The tile containing the statue
     */
    public void showContextMenu(StatueDetailsWrapper statue, double x, double y, Tile tile) {
        if (statue == null)
            return;

        boolean canUpgrade = statue.getLevel() < 3 && currentRunes >= statue.getUpgradePrice();
        boolean canUseAbility = currentEnergy > 0;

        contextMenu.updateForStatue(statue, canUpgrade, canUseAbility);

        Window window = rootNode.getScene().getWindow();
        contextMenu.show(window, x, y);
    }

    /**
     * Shows the side panel for a statue.
     * 
     * @param statue The statue details
     */
    public void showSidePanel(StatueDetailsWrapper statue) {
        if (statue == null)
            return;

        boolean canUpgrade = statue.getLevel() < 3 && currentRunes >= statue.getUpgradePrice();
        boolean canUseAbility = currentEnergy > 0;

        sidePanel.updateForStatue(statue, canUpgrade, canUseAbility);
        sidePanel.show();
    }

    /**
     * Hides the side panel.
     */
    public void hideSidePanel() {
        sidePanel.hide();
    }

    /**
     * Handles actions from the context menu.
     * 
     * @param action The action selected
     */
    private void handleContextMenuAction(String action) {
        // This will be implemented based on the statue type and action
        LOGGER.info("Context menu action: " + action);

        // Further implementation will be added for specific statue types
    }

    /**
     * Handles actions from the side panel.
     * 
     * @param action The action selected
     */
    private void handleSidePanelAction(String action) {
        // This will be implemented based on the statue type and action
        LOGGER.info("Side panel action: " + action);

        if ("close".equals(action)) {
            hideSidePanel();
        }

        // Further implementation will be added for specific statue types
    }

    /**
     * Shows confirmation for upgrading a statue.
     * 
     * @param statue The statue to upgrade
     * @param x      The x coordinate of the statue
     * @param y      The y coordinate of the statue
     */
    public void showUpgradeConfirmation(StatueDetailsWrapper statue, int x, int y) {
        confirmationDialog.forStatueUpgrade(statue, confirmed -> {
            if (confirmed) {
                // Dispatch upgrade event
                eventBus.publish(new UpgradeStatueUIEvent(statue.getId(), x, y));
            }
        });

        showDialogCentered(confirmationDialog);
    }

    /**
     * Shows confirmation for growing a tree (Freyr specific).
     * 
     * @param statue The Freyr statue
     * @param x      The x coordinate for the tree
     * @param y      The y coordinate for the tree
     */
    public void showGrowTreeConfirmation(StatueDetailsWrapper statue, int x, int y) {
        // For Freyr, we'll use all available energy (minimum 1)
        int energyCost = Math.max(1, currentEnergy);

        confirmationDialog.forGrowTree(statue, x, y, energyCost, confirmed -> {
            if (confirmed) {
                // Create parameters for Freyr's tree growing (x,y coordinates)
                String params = "tile:" + x + "," + y;

                // Dispatch use statue event with parameters
                eventBus.publish(new UseStatueUIEvent(x, y, statue.getId(), params));
            }
        });

        showDialogCentered(confirmationDialog);
    }

    /**
     * Shows confirmation for using a statue's blessing.
     * 
     * @param statue The statue
     * @param x      The x coordinate of the statue
     * @param y      The y coordinate of the statue
     */
    public void showBlessingConfirmation(StatueDetailsWrapper statue, int x, int y) {
        confirmationDialog.forStatueBlessing(statue, statue.getBlessing(), confirmed -> {
            if (confirmed) {
                // Create parameters for blessing (depends on statue type)
                String params = "blessing";

                // Dispatch use statue event
                eventBus.publish(new UseStatueUIEvent(x, y, statue.getId(), params));
            }
        });

        showDialogCentered(confirmationDialog);
    }

    /**
     * Highlights eligible river tiles for tree growing (Freyr specific).
     * 
     * @param riverTiles          The list of eligible tiles
     * @param onTileSelected Callback when a tile is selected
     */
    public void highlightEligibleRiverTiles(List<Tile> riverTiles, BiConsumer<Integer, Integer> onTileSelected) {
        tileHighlighter.highlightTiles(riverTiles, onTileSelected);
    }

    /**
     * Clears all highlighted tiles.
     */
    public void clearHighlightedTiles() {
        tileHighlighter.clearHighlights();
    }

    /**
     * Helper method to show a dialog centered in the window.
     * 
     * @param dialog The dialog to show
     */
    private void showDialogCentered(Popup dialog) {
        Window window = rootNode.getScene().getWindow();
        double centerX = window.getX() + window.getWidth() / 2 - dialog.getWidth() / 2;
        double centerY = window.getY() + window.getHeight() / 2 - dialog.getHeight() / 2;

        dialog.show(window, centerX, centerY);
    }
}
