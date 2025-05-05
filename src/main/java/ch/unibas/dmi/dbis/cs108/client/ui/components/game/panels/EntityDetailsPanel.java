package ch.unibas.dmi.dbis.cs108.client.ui.components.game.panels;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * A reusable side panel to display details of a game entity,
 * particularly focusing on statues for now.
 */
public class EntityDetailsPanel extends VBox {

    private final ResourceLoader resourceLoader;
    private ImageView entityImageView;
    private Label nameLabel;
    private Label descriptionLabel;
    private Label levelLabel; // Specific to upgradable entities like statues
    private Label effectsLabel; // Specific to statues or similar entities
    private ProgressBar levelProgress; // Specific to upgradable entities

    public EntityDetailsPanel(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        setPadding(new Insets(15));
        setSpacing(10);
        // Apply existing panel styles
        getStyleClass().add("entity-details-panel");
        initializeUI();
    }

    private void initializeUI() {
        entityImageView = new ImageView();
        entityImageView.setFitHeight(120);
        entityImageView.setFitWidth(80);
        entityImageView.setPreserveRatio(true);

        nameLabel = new Label();
        nameLabel.getStyleClass().add("entity-name-label"); // Reuse existing style

        descriptionLabel = new Label();
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("entity-description-label"); // Reuse existing style

        levelLabel = new Label();
        levelLabel.getStyleClass().add("entity-level-label"); // Reuse existing style

        levelProgress = new ProgressBar(0);
        levelProgress.getStyleClass().add("entity-level-progress"); // Reuse existing style

        effectsLabel = new Label();
        effectsLabel.setWrapText(true);
        effectsLabel.getStyleClass().add("entity-effects-label"); // Reuse existing style

        // Add elements, initially hidden or empty
        getChildren().addAll(entityImageView, nameLabel, descriptionLabel, levelLabel, levelProgress, effectsLabel);
        setVisible(false); // Initially hidden
    }

    /**
     * Updates the panel to display details of the given entity.
     *
     * @param entity The GameEntity to display. Can be null to hide the panel.
     */
    public void updateDetails(GameEntity entity) {
        if (entity == null) {
            setVisible(false);
            return;
        }

        nameLabel.setText(entity.getName());
        descriptionLabel.setText(entity.getDescription()); // Or getUsage() depending on context

        // Load image (assuming getCardImagePath or similar exists)
        String imagePath = ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry.getURL(entity.getId(), true);
        if (imagePath != null && !imagePath.isEmpty()) {
            Image img = resourceLoader.loadImage(imagePath);
            entityImageView.setImage(img);
            entityImageView.setVisible(true);
        } else {
            entityImageView.setVisible(false);
        }

        // Handle Statue specific details
        if (entity instanceof Statue statue) {
            levelLabel.setText("Level: " + statue.getLevel());
            levelLabel.setVisible(true);

            // Example progress: Level 1 = 0, Level 2 = 0.5, Level 3 = 1.0
            double progress = (statue.getLevel() - 1) / 2.0;
            levelProgress.setProgress(progress);
            levelProgress.setVisible(true);

            // Display effects based on level
            StringBuilder effectsText = new StringBuilder("Effects:\n");
            if (statue.getLevel() >= 2) {
                effectsText.append("- Deal: ").append(statue.getDeal()).append("\n"); // Assuming
                                                                                      // getDealDescription
                                                                                      // exists
            }
            if (statue.getLevel() >= 3) {
                effectsText.append("- Blessing: ").append(statue.getBlessing()).append("\n"); // Assuming
                                                                                              // getBlessingDescription
                                                                                              // exists
                effectsText.append("- Curse Chance: ")
                        .append(ch.unibas.dmi.dbis.cs108.SETTINGS.Config.CHANCE_FOR_CURSE.getValue()).append("%\n");
            }
            effectsLabel.setText(effectsText.toString());
            effectsLabel.setVisible(true);

        } else {
            // Hide statue-specific elements for other entity types
            levelLabel.setVisible(false);
            levelProgress.setVisible(false);
            effectsLabel.setVisible(false);
        }

        setVisible(true);
    }
}
