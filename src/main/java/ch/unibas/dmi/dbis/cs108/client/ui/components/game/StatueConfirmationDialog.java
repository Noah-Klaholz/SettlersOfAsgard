package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.StatueDetailsWrapper;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.StylesheetLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * A reusable confirmation dialog for statue-related actions.
 * This dialog can be customized for different statue types and actions,
 * such as placement, upgrade, or special abilities like Freyr's tree growing.
 */
public class StatueConfirmationDialog extends Popup {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(StatueConfirmationDialog.class.getName());

    /**
     * The main container for the dialog.
     */
    private final VBox container;
    /**
     * The label for the title of the dialog.
     */
    private final Label titleLabel;
    /**
     * The label for the description of the statue.
     */
    private final Label descriptionLabel;
    /**
     * The image view for displaying the statue's image.
     */
    private final ImageView imageView;
    /**
     * The label for displaying the cost of the statue.
     */
    private final Label costLabel;
    /**
     * The button for confirming the action.
     */
    private final Button confirmButton;
    /**
     * The button for canceling the action.
     */
    private final Button cancelButton;
    /**
     * The resource loader for loading images.
     */
    private final ResourceLoader resourceLoader;

    /**
     * The callback function to be called with the result of the confirmation.
     * It accepts a boolean indicating whether the action was confirmed or canceled.
     */
    public Consumer<Boolean> resultCallback;

    /**
     * Creates a new statue confirmation dialog.
     *
     * @param resourceLoader The resource loader for loading images
     */
    public StatueConfirmationDialog(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;

        // Setup main container
        container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setMinWidth(300);
        container.setMaxWidth(400);
        container.getStyleClass().add("dialog-container");

        // Ensure styles are loaded
        StylesheetLoader.loadCoreStylesheets(container);

        // Create UI components
        titleLabel = new Label();
        titleLabel.getStyleClass().add("dialog-title");
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        descriptionLabel = new Label();
        descriptionLabel.getStyleClass().add("dialog-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(120);

        HBox imageBox = new HBox();
        imageBox.setAlignment(Pos.CENTER);
        imageBox.getChildren().add(imageView);

        costLabel = new Label();
        costLabel.getStyleClass().add("dialog-cost");

        // Buttons
        confirmButton = new Button("Confirm");
        confirmButton.getStyleClass().add("dialog-confirm-button");
        confirmButton.setOnAction(e -> {
            if (resultCallback != null) {
                resultCallback.accept(true);
            }
            hide();
        });

        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("dialog-cancel-button");
        cancelButton.setOnAction(e -> {
            if (resultCallback != null) {
                resultCallback.accept(false);
            }
            hide();
        });

        HBox buttonBox = new HBox(10, confirmButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Add spacer between content and buttons
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Add components to container
        container.getChildren().addAll(
                titleLabel,
                imageBox,
                descriptionLabel,
                costLabel,
                spacer,
                buttonBox);

        // Set dialog properties
        setAutoHide(false);
        getContent().add(container);
    }

    /**
     * Configures the dialog for statue placement.
     *
     * @param statue   The statue to be placed
     * @param x        The x coordinate for placement
     * @param y        The y coordinate for placement
     * @param callback Called with true if confirmed, false otherwise
     * @return This dialog for method chaining
     */
    public StatueConfirmationDialog forStatuePlacement(StatueDetailsWrapper statue, int x, int y,
                                                       Consumer<Boolean> callback) {
        this.resultCallback = callback;

        titleLabel.setText("Place " + statue.getName() + "?");
        descriptionLabel.setText("Are you sure you want to place " + statue.getName() +
                " at position (" + x + ", " + y + ")?\n\n" + statue.getDescription());

        Image image = statue.getCardImage();
        if (image != null && !image.isError()) {
            imageView.setImage(image);
            imageView.setVisible(true);
        } else {
            imageView.setVisible(false);
        }

        costLabel.setText("Price: " + statue.getPrice() + " Runes");
        costLabel.setVisible(statue.getPrice() > 0);

        confirmButton.setText("Place Statue");

        return this;
    }

    /**
     * Configures the dialog for growing a tree (specific to Freyr).
     *
     * @param statue     The Freyr statue
     * @param x          The x coordinate for tree placement
     * @param y          The y coordinate for tree placement
     * @param energyCost The energy cost to grow the tree
     * @param callback   Called with true if confirmed, false otherwise
     * @return This dialog for method chaining
     */
    public StatueConfirmationDialog forGrowTree(StatueDetailsWrapper statue, int x, int y, int energyCost,
                                                Consumer<Boolean> callback) {
        this.resultCallback = callback;

        titleLabel.setText("Grow Tree with " + statue.getName());
        descriptionLabel.setText("Are you sure you want to use " + statue.getName() +
                " to grow a tree at (" + x + ", " + y + ")?\n\nThis will consume " +
                energyCost + " energy.");

        Image image = statue.getCardImage();
        if (image != null && !image.isError()) {
            imageView.setImage(image);
            imageView.setVisible(true);
        } else {
            imageView.setVisible(false);
        }

        costLabel.setText("Energy Cost: " + energyCost);
        costLabel.setVisible(true);

        confirmButton.setText("Grow Tree");

        return this;
    }

    /**
     * Configures the dialog for upgrading a statue.
     *
     * @param statue   The statue to upgrade
     * @param callback Called with true if confirmed, false otherwise
     * @return This dialog for method chaining
     */
    public StatueConfirmationDialog forStatueUpgrade(StatueDetailsWrapper statue, Consumer<Boolean> callback) {
        this.resultCallback = callback;

        titleLabel.setText("Upgrade " + statue.getName() + "?");

        int currentLevel = statue.getLevel();
        String nextLevelEffect;

        if (currentLevel == 1) {
            nextLevelEffect = "Deal: " + statue.getDeal();
        } else if (currentLevel == 2) {
            nextLevelEffect = "Blessing: " + statue.getBlessing() + "\n\nPossible Curse: " + statue.getCurse();
        } else {
            nextLevelEffect = "Maximum level reached";
        }

        descriptionLabel.setText("Upgrade " + statue.getName() + " to level " + (currentLevel + 1) +
                "?\n\nNext level effect:\n" + nextLevelEffect);

        Image image = statue.getCardImage();
        if (image != null && !image.isError()) {
            imageView.setImage(image);
            imageView.setVisible(true);
        } else {
            imageView.setVisible(false);
        }

        costLabel.setText("Upgrade Cost: " + statue.getUpgradePrice() + " Runes");
        costLabel.setVisible(true);

        confirmButton.setText("Upgrade");

        return this;
    }

    /**
     * Configures the dialog for using a statue's blessing.
     *
     * @param statue      The statue to use
     * @param description Description of the blessing effect
     * @param callback    Called with true if confirmed, false otherwise
     * @return This dialog for method chaining
     */
    public StatueConfirmationDialog forStatueBlessing(StatueDetailsWrapper statue, String description,
                                                      Consumer<Boolean> callback) {
        this.resultCallback = callback;

        titleLabel.setText("Use " + statue.getName() + " Blessing?");
        descriptionLabel
                .setText("Are you sure you want to use the blessing of " + statue.getName() + "?\n\n" + description);

        Image image = statue.getCardImage();
        if (image != null && !image.isError()) {
            imageView.setImage(image);
            imageView.setVisible(true);
        } else {
            imageView.setVisible(false);
        }

        costLabel.setVisible(false);

        confirmButton.setText("Use Blessing");

        return this;
    }

    /**
     * Sets custom button text for the confirmation button.
     *
     * @param text The text to display on the confirmation button
     * @return This dialog for method chaining
     */
    public StatueConfirmationDialog withConfirmButtonText(String text) {
        confirmButton.setText(text);
        return this;
    }

    /**
     * Sets custom button text for the cancel button.
     *
     * @param text The text to display on the cancel button
     * @return This dialog for method chaining
     */
    public StatueConfirmationDialog withCancelButtonText(String text) {
        cancelButton.setText(text);
        return this;
    }

    /**
     * Sets a custom description for the dialog.
     *
     * @param description The description text
     * @return This dialog for method chaining
     */
    public StatueConfirmationDialog withDescription(String description) {
        descriptionLabel.setText(description);
        return this;
    }

    /**
     * Sets a custom title for the dialog.
     *
     * @param title The title text
     * @return This dialog for method chaining
     */
    public StatueConfirmationDialog withTitle(String title) {
        titleLabel.setText(title);
        return this;
    }

    /**
     * Sets a custom cost label for the dialog.
     *
     * @param costText The cost text
     * @return This dialog for method chaining
     */
    public StatueConfirmationDialog withCost(String costText) {
        costLabel.setText(costText);
        costLabel.setVisible(true);
        return this;
    }

    /**
     * Hides the cost label.
     *
     * @return This dialog for method chaining
     */
    public StatueConfirmationDialog withoutCost() {
        costLabel.setVisible(false);
        return this;
    }
}
