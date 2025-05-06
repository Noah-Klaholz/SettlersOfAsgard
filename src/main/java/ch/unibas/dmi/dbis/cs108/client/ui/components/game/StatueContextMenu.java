package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.StatueDetailsWrapper;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.StylesheetLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * A context menu for statue interactions that displays available actions
 * based on the statue's level and abilities.
 */
public class StatueContextMenu extends Popup {
    private static final Logger LOGGER = Logger.getLogger(StatueContextMenu.class.getName());

    private final VBox container;
    private Consumer<String> actionHandler;

    /**
     * Constructs a new StatueContextMenu.
     */
    public StatueContextMenu() {
        container = new VBox(5);
        container.setPadding(new Insets(10));
        container.getStyleClass().add("contextual-menu");

        // Make sure to use existing styles
        StylesheetLoader.loadCoreStylesheets(container);
        StylesheetLoader.loadStylesheet(container, "/css/game-screen.css");

        setAutoHide(true);
        getContent().add(container);
    }

    /**
     * Updates the menu with actions appropriate for the given statue and its
     * current state.
     *
     * @param statue        The statue details wrapper
     * @param canUpgrade    True if the player has enough resources to upgrade
     * @param canUseAbility True if the statue ability can be used
     */
    public void updateForStatue(StatueDetailsWrapper statue, boolean canUpgrade, boolean canUseAbility) {
        container.getChildren().clear();

        if (statue == null) {
            LOGGER.warning("Cannot update context menu: Statue is null");
            return;
        }

        // Add statue name as header
        Label nameLabel = new Label(statue.getName());
        nameLabel.getStyleClass().add("menu-header");
        container.getChildren().add(nameLabel);

        // Add upgrade button if appropriate for the statue's level
        if (statue.getLevel() < 3) {
            Button upgradeButton = new Button("Upgrade to Level " + (statue.getLevel() + 1) +
                    " (" + statue.getUpgradePrice() + " Runes)");
            upgradeButton.getStyleClass().add("menu-button");

            if (!canUpgrade) {
                upgradeButton.getStyleClass().add("disabled-button");
                upgradeButton.setDisable(true);
            }

            upgradeButton.setOnAction(e -> {
                if (actionHandler != null) {
                    actionHandler.accept("upgrade");
                }
                hide();
            });

            container.getChildren().add(upgradeButton);
        }

        // Add ability buttons based on the statue's level
        if (statue.hasDeployableDeal()) {
            // Create Deal button (Freyr: Grow Tree)
            String actionName = statue.getId() == 31 ? "Grow Tree" : "Use Deal";
            Button dealButton = new Button(actionName);
            dealButton.getStyleClass().add("menu-button");

            if (!canUseAbility) {
                dealButton.getStyleClass().add("disabled-button");
                dealButton.setDisable(true);
            }

            dealButton.setOnAction(e -> {
                if (actionHandler != null) {
                    // For Freyr, we use a special action name
                    if (statue.getId() == 31) {
                        actionHandler.accept("grow_tree");
                    } else {
                        actionHandler.accept("deal");
                    }
                }
                hide();
            });

            container.getChildren().add(dealButton);
        }

        if (statue.hasDeployableBlessing()) {
            // For Freyr, this would be "Grow Trees on All River Tiles"
            String actionName = statue.getId() == 31 ? "Grow Trees on All River Tiles" : "Use Blessing";
            Button blessingButton = new Button(actionName);
            blessingButton.getStyleClass().add("menu-button");

            if (!canUseAbility) {
                blessingButton.getStyleClass().add("disabled-button");
                blessingButton.setDisable(true);
            }

            blessingButton.setOnAction(e -> {
                if (actionHandler != null) {
                    actionHandler.accept("blessing");
                }
                hide();
            });

            container.getChildren().add(blessingButton);
        }

        // Add info button
        Button infoButton = new Button("Info");
        infoButton.getStyleClass().add("menu-button");
        infoButton.setOnAction(e -> {
            if (actionHandler != null) {
                actionHandler.accept("info");
            }
            hide();
        });
        container.getChildren().add(infoButton);

        // Add cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("menu-button");
        cancelButton.setOnAction(e -> hide());
        container.getChildren().add(cancelButton);
    }

    /**
     * Updates the menu with Freyr-specific options for tree growing.
     *
     * @param statue        The Freyr statue wrapper
     * @param canUseAbility True if the player has enough energy
     */
    public void updateForFreyr(StatueDetailsWrapper statue, boolean canUseAbility) {
        container.getChildren().clear();

        // Add statue name as header
        Label nameLabel = new Label(statue.getName());
        nameLabel.getStyleClass().add("menu-header");
        container.getChildren().add(nameLabel);

        // Add upgrade button if appropriate
        if (statue.getLevel() < 3) {
            Button upgradeButton = new Button("Upgrade to Level " + (statue.getLevel() + 1) +
                    " (" + statue.getUpgradePrice() + " Runes)");
            upgradeButton.getStyleClass().add("menu-button");

            upgradeButton.setOnAction(e -> {
                if (actionHandler != null) {
                    actionHandler.accept("upgrade");
                }
                hide();
            });

            container.getChildren().add(upgradeButton);
        }

        // Add "Grow Tree" button (Freyr's special ability)
        Button growTreeButton = new Button("Grow Tree");
        growTreeButton.getStyleClass().add("menu-button");

        if (!canUseAbility) {
            growTreeButton.getStyleClass().add("disabled-button");
            growTreeButton.setDisable(true);
        }

        growTreeButton.setOnAction(e -> {
            if (actionHandler != null) {
                actionHandler.accept("grow_tree");
            }
            hide();
        });

        container.getChildren().add(growTreeButton);

        // Add info button
        Button infoButton = new Button("Info");
        infoButton.getStyleClass().add("menu-button");
        infoButton.setOnAction(e -> {
            if (actionHandler != null) {
                actionHandler.accept("info");
            }
            hide();
        });
        container.getChildren().add(infoButton);

        // Add cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("menu-button");
        cancelButton.setOnAction(e -> hide());
        container.getChildren().add(cancelButton);
    }

    /**
     * Sets a handler for menu item clicks.
     *
     * @param handler Consumer that accepts the action name
     */
    public void setActionHandler(Consumer<String> handler) {
        this.actionHandler = handler;
    }
}
