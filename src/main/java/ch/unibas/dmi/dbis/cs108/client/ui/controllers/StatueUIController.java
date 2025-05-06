package ch.unibas.dmi.dbis.cs108.client.ui.controllers;

import ch.unibas.dmi.dbis.cs108.client.ui.components.game.StatueActionFeedback;
import ch.unibas.dmi.dbis.cs108.client.ui.components.game.StatueConfirmationDialog;
import ch.unibas.dmi.dbis.cs108.client.ui.components.game.StatueInteractionHandler;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.PlaceStatueResponseEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.PlaceStatueUIEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.StatueDetailsWrapper;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * Controller for managing statue-related UI components and interactions.
 * This centralizes all statue UI logic in one place.
 */
public class StatueUIController {
    private static final Logger LOGGER = Logger.getLogger(StatueUIController.class.getName());

    private final UIEventBus eventBus;
    private final ResourceLoader resourceLoader;
    private final Pane rootPane;

    private StatueInteractionHandler interactionHandler;
    private StatueActionFeedback actionFeedback;

    private Player currentPlayer;

    /**
     * Creates a new statue UI controller.
     *
     * @param eventBus       The UI event bus
     * @param resourceLoader The resource loader
     * @param rootPane       The root pane for the UI
     */
    public StatueUIController(UIEventBus eventBus, ResourceLoader resourceLoader, Pane rootPane) {
        this.eventBus = eventBus;
        this.resourceLoader = resourceLoader;
        this.rootPane = rootPane;

        initializeComponents();
        subscribeToEvents();
    }

    /**
     * Initializes UI components.
     */
    private void initializeComponents() {
        interactionHandler = new StatueInteractionHandler(eventBus, resourceLoader, rootPane);
        actionFeedback = new StatueActionFeedback(rootPane);
    }

    /**
     * Subscribes to relevant events from the UI event bus.
     */
    private void subscribeToEvents() {
        eventBus.subscribe(PlaceStatueResponseEvent.class, this::handlePlaceStatueResponse);
        // Subscribe to other events as needed
    }

    /**
     * Updates the player information.
     *
     * @param player The current player
     */
    public void updatePlayerInfo(Player player) {
        this.currentPlayer = player;

        if (player != null) {
            interactionHandler.updatePlayerInfo(player, player.getRunes(), player.getEnergy());
        }
    }

    /**
     * Shows the context menu for a statue.
     *
     * @param statue The statue
     * @param x      The x screen coordinate for the menu
     * @param y      The y screen coordinate for the menu
     * @param tile   The tile containing the statue
     */
    public void showStatueContextMenu(Statue statue, double x, double y, Tile tile) {
        if (statue == null)
            return;

        StatueDetailsWrapper wrapper = StatueDetailsWrapper.forStatue(statue, resourceLoader);
        interactionHandler.showContextMenu(wrapper, x, y, tile);
    }

    /**
     * Shows the side panel for a statue.
     *
     * @param statue The statue
     */
    public void showStatueSidePanel(Statue statue) {
        if (statue == null)
            return;

        StatueDetailsWrapper wrapper = StatueDetailsWrapper.forStatue(statue, resourceLoader);
        interactionHandler.showSidePanel(wrapper);
    }

    /**
     * Shows the confirmation dialog for placing a statue.
     *
     * @param statue The statue to place
     * @param x      The x coordinate
     * @param y      The y coordinate
     */
    public void showPlaceStatueConfirmation(Statue statue, int x, int y) {
        if (statue == null)
            return;

        StatueDetailsWrapper wrapper = StatueDetailsWrapper.forStatue(statue, resourceLoader);

        StatueConfirmationDialog dialog = new StatueConfirmationDialog(resourceLoader);
        dialog.forStatuePlacement(wrapper, x, y, confirmed -> {
            if (confirmed) {
                eventBus.publish(new PlaceStatueUIEvent(x, y, statue.getId()));
            }
        });

        showDialogCentered(dialog);
    }

    /**
     * Shows the confirmation dialog for upgrading a statue.
     *
     * @param statue The statue to upgrade
     * @param x      The x coordinate
     * @param y      The y coordinate
     */
    public void showUpgradeStatueConfirmation(Statue statue, int x, int y) {
        if (statue == null)
            return;

        StatueDetailsWrapper wrapper = StatueDetailsWrapper.forStatue(statue, resourceLoader);
        interactionHandler.showUpgradeConfirmation(wrapper, x, y);
    }

    /**
     * Shows eligible river tiles for growing trees (Freyr specific).
     *
     * @param statue         The Freyr statue
     * @param riverTiles     The list of river tiles
     * @param onTileSelected Callback when a tile is selected
     */
    public void showEligibleRiverTilesForTreeGrowing(Statue statue, List<Tile> riverTiles,
                                                     BiConsumer<Integer, Integer> onTileSelected) {
        if (statue == null || statue.getId() != 31)
            return; // 31 is Freyr's ID

        interactionHandler.highlightEligibleRiverTiles(riverTiles, (x, y) -> {
            // When a river tile is selected, show confirmation dialog
            StatueDetailsWrapper wrapper = StatueDetailsWrapper.forStatue(statue, resourceLoader);
            interactionHandler.showGrowTreeConfirmation(wrapper, x, y);

            if (onTileSelected != null) {
                onTileSelected.accept(x, y);
            }
        });
    }

    /**
     * Handles the response to placing a statue.
     *
     * @param event The place statue response event
     */
    private void handlePlaceStatueResponse(PlaceStatueResponseEvent event) {
        Platform.runLater(() -> {
            if (event.isSuccess()) {
                actionFeedback.showSuccessNotification("Statue placed successfully!");

                // Get the statue and show its side panel
                try {
                    int statueId = Integer.parseInt(event.getStatueId());
                    Statue statue = EntityRegistry.getStatue(statueId);
                    if (statue != null) {
                        showStatueSidePanel(statue);
                    }
                } catch (NumberFormatException e) {
                    LOGGER.warning("Invalid statue ID: " + event.getStatueId());
                }
            } else {
                actionFeedback.showErrorNotification("Failed to place statue: " + event.getMessage());
            }
        });
    }

    /**
     * Helper method to show a dialog centered in the window.
     *
     * @param dialog The dialog to show
     */
    private void showDialogCentered(StatueConfirmationDialog dialog) {
        if (rootPane.getScene() == null || rootPane.getScene().getWindow() == null) {
            LOGGER.warning("Cannot show dialog: Scene or window is null");
            return;
        }

        dialog.show(rootPane.getScene().getWindow());
    }

    /**
     * Cleans up resources when the controller is no longer needed.
     */
    public void cleanup() {
        eventBus.unsubscribe(PlaceStatueResponseEvent.class, this::handlePlaceStatueResponse);
        // Unsubscribe from other events as needed
    }
}
