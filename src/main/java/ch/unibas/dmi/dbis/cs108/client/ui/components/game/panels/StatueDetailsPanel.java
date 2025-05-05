package ch.unibas.dmi.dbis.cs108.client.ui.components.game.panels;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.logging.Logger;

/**
 * A specialized panel for displaying statue details with level progress,
 * effects at different levels, upgrade costs, and action capabilities.
 * Designed to be reusable for any statue.
 */
public class StatueDetailsPanel extends VBox {
    private static final Logger LOGGER = Logger.getLogger(StatueDetailsPanel.class.getName());
    private final ResourceLoader resourceLoader;

    // UI Components
    private final ImageView statueImageView;
    private final Label nameLabel;
    private final Label levelLabel;
    private final ProgressBar levelProgressBar;
    private final Label descriptionLabel;
    private final VBox effectsBox;
    private final Label upgradeCostLabel;

    /**
     * Creates a new StatueDetailsPanel with all necessary UI elements.
     * The panel starts hidden until populated with a statue.
     *
     * @param resourceLoader ResourceLoader to load statue images
     */
    public StatueDetailsPanel(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;

        // Set up the panel styling
        setPadding(new Insets(15));
        setSpacing(10);
        setMaxWidth(300);
        getStyleClass().add("entity-details-panel");

        // Create components
        statueImageView = new ImageView();
        statueImageView.setFitHeight(120);
        statueImageView.setPreserveRatio(true);

        HBox imageContainer = new HBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.getChildren().add(statueImageView);

        nameLabel = new Label();
        nameLabel.getStyleClass().add("entity-name-label");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);

        HBox nameLevelBox = new HBox(10);
        nameLevelBox.setAlignment(Pos.CENTER_LEFT);

        levelLabel = new Label();
        levelLabel.getStyleClass().add("entity-level-label");

        nameLevelBox.getChildren().addAll(nameLabel, levelLabel);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        levelProgressBar = new ProgressBar(0);
        levelProgressBar.getStyleClass().add("entity-level-progress");
        levelProgressBar.setPrefWidth(Double.MAX_VALUE);

        descriptionLabel = new Label();
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("entity-description-label");

        effectsBox = new VBox(5);
        effectsBox.getStyleClass().add("effects-container");

        upgradeCostLabel = new Label();
        upgradeCostLabel.getStyleClass().add("upgrade-cost-label");

        // Add all components to the panel
        getChildren().addAll(
                imageContainer,
                nameLevelBox,
                levelProgressBar,
                new Separator(),
                descriptionLabel,
                new Separator(),
                new Label("Effects:"),
                effectsBox,
                upgradeCostLabel);

        // Initially hidden
        setVisible(false);
    }

    /**
     * Updates the panel to display information for the given statue.
     * If null, hides the panel.
     * 
     * @param statue The statue to display, or null to hide the panel
     */
    public void updateStatue(Statue statue) {
        if (statue == null) {
            setVisible(false);
            return;
        }

        // Update statue image
        String imagePath = ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry.getURL(statue.getId(), true);
        if (imagePath != null && !imagePath.isEmpty()) {
            Image img = resourceLoader.loadImage(imagePath);
            if (img != null && !img.isError()) {
                statueImageView.setImage(img);
                statueImageView.setVisible(true);
            } else {
                statueImageView.setVisible(false);
            }
        } else {
            statueImageView.setVisible(false);
        }

        // Update name and level
        nameLabel.setText(statue.getName());
        int level = statue.getLevel();
        levelLabel.setText("Level " + level);

        // Update progress bar - Level 1: 0%, Level 2: 50%, Level 3: 100%
        double progress = (level - 1) / 2.0;
        levelProgressBar.setProgress(progress);

        // Update description
        descriptionLabel.setText(statue.getDescription());

        // Update effects list
        effectsBox.getChildren().clear();

        // Add level-appropriate effects
        if (level >= 1) {
            Label placeholderLabel = new Label("Level 1: Placed in the world");
            placeholderLabel.getStyleClass().add("effect-description");
            effectsBox.getChildren().add(placeholderLabel);
        }

        if (level >= 2) {
            Label dealLabel = new Label("Level 2: Deal - " + statue.getDeal());
            dealLabel.getStyleClass().add("effect-description");
            dealLabel.setWrapText(true);
            effectsBox.getChildren().add(dealLabel);
        } else if (statue.getDeal() != null && !statue.getDeal().isEmpty()) {
            Label lockedDealLabel = new Label("Level 2 (Locked): Deal - " + statue.getDeal());
            lockedDealLabel.getStyleClass().addAll("effect-description", "locked-effect");
            lockedDealLabel.setWrapText(true);
            effectsBox.getChildren().add(lockedDealLabel);
        }

        if (level >= 3) {
            Label blessingLabel = new Label("Level 3: Blessing - " + statue.getBlessing());
            blessingLabel.getStyleClass().add("effect-description");
            blessingLabel.setWrapText(true);
            effectsBox.getChildren().add(blessingLabel);

            Label curseLabel = new Label("Level 3: Curse - " + statue.getCurse() +
                    " (Risk: " + SETTINGS.Config.CHANCE_FOR_CURSE.getValue() + "%)");
            curseLabel.getStyleClass().addAll("effect-description", "curse-effect");
            curseLabel.setWrapText(true);
            effectsBox.getChildren().add(curseLabel);
        } else if (statue.getBlessing() != null && !statue.getBlessing().isEmpty()) {
            Label lockedBlessingLabel = new Label("Level 3 (Locked): Blessing & Curse");
            lockedBlessingLabel.getStyleClass().addAll("effect-description", "locked-effect");
            lockedBlessingLabel.setWrapText(true);
            effectsBox.getChildren().add(lockedBlessingLabel);
        }

        // Update upgrade cost
        if (level < 3) {
            upgradeCostLabel.setText("Upgrade Cost: " + statue.getUpgradePrice() + " Runes");
            upgradeCostLabel.setVisible(true);
        } else {
            upgradeCostLabel.setText("Maximum Level Reached");
            upgradeCostLabel.setVisible(true);
        }

        setVisible(true);
    }
}
