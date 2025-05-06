package ch.unibas.dmi.dbis.cs108.client.ui.components.game;

import ch.unibas.dmi.dbis.cs108.client.ui.utils.CardDetails;
import ch.unibas.dmi.dbis.cs108.client.ui.utils.ResourceLoader;
import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class StatueSelectionPopup extends Popup {
    private static final Logger LOGGER = Logger.getLogger(StatueSelectionPopup.class.getName());
    private final ResourceLoader resourceLoader;
    private final Consumer<CardDetails> onStatueSelected;
    private final List<Integer> statueIds;

    public StatueSelectionPopup(ResourceLoader resourceLoader, Consumer<CardDetails> onStatueSelected) {
        this.resourceLoader = resourceLoader;
        this.onStatueSelected = onStatueSelected;
        this.statueIds = new ArrayList<>();

        // Add the other statue IDs (example IDs, replace with actual ones)
        for (int i = 30; i <= 37; i++) {
            this.statueIds.add(i);
        }

        initializeUI();
    }

    private void initializeUI() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: #333333; -fx-border-color: #555555; -fx-border-width: 2px; -fx-border-radius: 5px;");
        container.setMaxHeight(400);
        container.setMaxWidth(350);

        Label title = new Label("Select a Statue");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox statueList = new VBox(8);
        statueList.setPadding(new Insets(5));

        for (Integer statueId : statueIds) {
            HBox statueRow = createStatueRow(statueId);
            statueList.getChildren().add(statueRow);
        }

        scrollPane.setContent(statueList);

        Button closeButton = new Button("Cancel");
        closeButton.setOnAction(e -> this.hide());

        container.getChildren().addAll(title, scrollPane, closeButton);

        this.getContent().add(container);
        this.setAutoHide(true);
    }

    private HBox createStatueRow(int statueId) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5));
        row.setStyle("-fx-background-color: #444444; -fx-border-radius: 3px;");

        GameEntity entity = EntityRegistry.getGameEntityOriginalById(statueId);
        String world = "UNKNOWN";
        if (entity == null) {
            LOGGER.warning("Entity with ID " + statueId + " not found.");
            return row; // Return empty row if entity is not found
        } else if (!(entity instanceof Statue statue)) {
            LOGGER.warning("Entity with ID " + statueId + " is not a statue.");
            return row; // Return empty row if entity is not a statue
        } else {
            world = statue.getWorld();
        }
        String imageUrl = EntityRegistry.getURL(statueId, true);
        String title = entity.getName();
        String description = entity.getUsage();
        int price = entity.getPrice();

        // Create CardDetails for this statue
        CardDetails cardDetails = new CardDetails(statueId, title, description, entity.getDescription(), imageUrl, price);

        // Miniature image
        ImageView imageView = new ImageView();
        imageView.setFitHeight(60);
        imageView.setFitWidth(40);
        imageView.setPreserveRatio(true);

        Image image = resourceLoader.loadImage(imageUrl);
        if (image != null && !image.isError()) {
            imageView.setImage(image);
        } else {
            // Placeholder for missing image
            StackPane placeholder = new StackPane();
            placeholder.setMinSize(40, 60);
            placeholder.setStyle("-fx-background-color: #666666;");
            row.getChildren().add(placeholder);
        }

        // Statue info
        VBox infoBox = new VBox(2);
        Label nameLabel = new Label(title);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        Label priceLabel = new Label("Price: " + price + " runes");
        priceLabel.setStyle("-fx-text-fill: #aaaaaa;");
        Label worldLabel = new Label("World: " + world);
        worldLabel.setStyle("-fx-text-fill: white;");
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: white;");
        descLabel.setWrapText(true);

        infoBox.getChildren().addAll(nameLabel, priceLabel, worldLabel, descLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        row.getChildren().addAll(imageView, infoBox);

        // Make the entire row clickable
        row.setOnMouseClicked(e -> {
            onStatueSelected.accept(cardDetails);
            this.hide();
        });

        // Hover effect
        row.setOnMouseEntered(e ->
                row.setStyle("-fx-background-color: #555555; -fx-border-radius: 3px;"));
        row.setOnMouseExited(e ->
                row.setStyle("-fx-background-color: #444444; -fx-border-radius: 3px;"));

        return row;
    }
}