package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.StatueDetailsWrapper;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.StylesheetLoader;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * A side panel that displays detailed information about a statue,
 * including its level, effects, and available actions.
 */
public class StatueSidePanel extends VBox {
    /**
     * Logger for StatueSidePanel.
     */
    private static final Logger LOGGER = Logger.getLogger(StatueSidePanel.class.getName());

    /**
     * The default width of the side panel.
     */
    private final Label nameLabel;
    /**
     * The label displaying the name of the statue.
     */
    private final ImageView imageView;
    /**
     * The image view displaying the statue's image.
     */
    private final Label descriptionLabel;
    /**
     * The label displaying the description of the statue.
     */
    private final Label worldLabel;
    /**
     * The label displaying the world of the statue.
     */
    private final Label levelLabel;
    /**
     * The label displaying the level of the statue.
     */
    private final ProgressBar levelProgress;
    /**
     * The progress bar showing the statue's level progress.
     */
    private final VBox effectsBox;
    /**
     * The box displaying the effects of the statue.
     */
    private final Button upgradeButton;
    /**
     * The button for upgrading the statue.
     */
    private final Button useAbilityButton;
    /**
     * The button for using the statue's ability.
     */
    private final Button closeButton;
    /**
     * The button for closing the side panel.
     */

    /* * The current statue being displayed in the side panel.
     */
    private StatueDetailsWrapper currentStatue;
    /**
     * The action handler for button actions.
     */
    private Consumer<String> actionHandler;

    /**
     * Creates a new statue side panel.
     */
    public StatueSidePanel() {
        super(15);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_CENTER);
        setMinWidth(250);
        setMaxWidth(320);
        getStyleClass().add("statue-side-panel");

        // Make sure we use existing styles
        StylesheetLoader.loadCoreStylesheets(this);

        // Create UI elements
        nameLabel = new Label();
        nameLabel.getStyleClass().add("statue-name");

        imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setFitWidth(120);
        imageView.setPreserveRatio(true);

        worldLabel = new Label();
        worldLabel.getStyleClass().add("statue-world");

        descriptionLabel = new Label();
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("statue-description");

        levelLabel = new Label();
        levelLabel.getStyleClass().add("statue-level");

        levelProgress = new ProgressBar(0);
        levelProgress.getStyleClass().add("level-progress");
        levelProgress.setMaxWidth(Double.MAX_VALUE);

        effectsBox = new VBox(8);
        effectsBox.getStyleClass().add("effects-box");

        // Create action buttons
        upgradeButton = new Button("Upgrade");
        upgradeButton.getStyleClass().add("statue-action-button");
        upgradeButton.setOnAction(e -> {
            if (actionHandler != null) {
                actionHandler.accept("upgrade");
            }
        });

        useAbilityButton = new Button("Use Ability");
        useAbilityButton.getStyleClass().add("statue-action-button");
        useAbilityButton.setOnAction(e -> {
            if (actionHandler != null) {
                if (currentStatue != null && currentStatue.getId() == 31) {
                    actionHandler.accept("grow_tree");
                } else {
                    actionHandler.accept("use_ability");
                }
            }
        });

        closeButton = new Button("Close");
        closeButton.getStyleClass().add("statue-action-button");
        closeButton.setOnAction(e -> {
            if (actionHandler != null) {
                actionHandler.accept("close");
            }
        });

        // Add components to panel
        HBox buttonBox = new HBox(10, upgradeButton, useAbilityButton);
        buttonBox.setAlignment(Pos.CENTER);

        getChildren().addAll(
                nameLabel,
                imageView,
                worldLabel,
                new Separator(),
                levelLabel,
                levelProgress,
                new Separator(),
                descriptionLabel,
                new Separator(),
                effectsBox,
                buttonBox,
                closeButton);

        // Initially hide the panel
        setVisible(false);
        setManaged(false);
    }

    /**
     * Updates the panel with information about the given statue.
     *
     * @param statue        The statue to display
     * @param canUpgrade    Whether the player has enough resources to upgrade
     * @param canUseAbility Whether the statue's ability can be used
     */
    public void updateForStatue(StatueDetailsWrapper statue, boolean canUpgrade, boolean canUseAbility) {
        if (statue == null) {
            LOGGER.warning("Cannot update side panel: Statue is null");
            return;
        }

        this.currentStatue = statue;

        nameLabel.setText(statue.getName());

        if (statue.getCardImage() != null) {
            imageView.setImage(statue.getCardImage());
            imageView.setVisible(true);
            imageView.setManaged(true);
        } else {
            imageView.setVisible(false);
            imageView.setManaged(false);
        }

        descriptionLabel.setText(statue.getDescription().isEmpty() ? statue.getUsage() : statue.getDescription());

        worldLabel.setText("World: " + statue.getWorld());

        // Update level information
        int level = statue.getLevel();
        levelLabel.setText("Level: " + level + " / 3");

        // Level progress bar shows progress through levels 1-3
        levelProgress.setProgress((level - 1) / 2.0);

        // Update effects box
        effectsBox.getChildren().clear();
        Label effectsTitle = new Label("Effects:");
        effectsTitle.getStyleClass().add("effects-title");
        effectsBox.getChildren().add(effectsTitle);

        if (level >= 1 && !statue.getDeal().isEmpty()) {
            Label dealLabel = new Label("Deal: " + statue.getDeal());
            dealLabel.getStyleClass().add("effect-detail");
            effectsBox.getChildren().add(dealLabel);
        }

        if (level >= 2 && !statue.getBlessing().isEmpty()) {
            Label blessingLabel = new Label("Blessing: " + statue.getBlessing());
            blessingLabel.getStyleClass().add("effect-detail");
            effectsBox.getChildren().add(blessingLabel);
        }

        if (level >= 3 && !statue.getCurse().isEmpty()) {
            Label curseLabel = new Label("Curse: " + statue.getCurse());
            curseLabel.getStyleClass().add("effect-detail");
            curseLabel.setTextFill(Color.RED); // Curse is highlighted in red
            effectsBox.getChildren().add(curseLabel);
        }

        // Update buttons
        upgradeButton.setText("Upgrade (" + statue.getUpgradePrice() + " Runes)");
        upgradeButton.setDisable(!canUpgrade || level >= 3);
        upgradeButton.setVisible(level < 3);
        upgradeButton.setManaged(level < 3);

        // Customize ability button text for Freyr
        if (statue.getId() == 31) {
            useAbilityButton.setText("Grow Tree");
        } else {
            useAbilityButton.setText(level >= 2 ? "Use Blessing" : "Use Deal");
        }

        useAbilityButton.setDisable(!canUseAbility);
    }

    /**
     * Shows the panel with a fade-in animation.
     */
    public void show() {
        setVisible(true);
        setManaged(true);
        setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    /**
     * Hides the panel with a fade-out animation.
     */
    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            setVisible(false);
            setManaged(false);
        });
        fadeOut.play();
    }

    /**
     * Sets a handler for button actions.
     *
     * @param handler Consumer that accepts the action name
     */
    public void setActionHandler(Consumer<String> handler) {
        this.actionHandler = handler;
    }

    /**
     * Custom separator using CSS styling.
     */
    private static class Separator extends Region {
        public Separator() {
            setPrefHeight(1);
            setMaxWidth(Double.MAX_VALUE);
            getStyleClass().add("statue-separator");
            setBackground(new Background(new BackgroundFill(
                    Color.gray(0.8), CornerRadii.EMPTY, Insets.EMPTY)));
            setMargin(this, new Insets(5, 0, 5, 0));
        }
    }
}
