package ch.unibas.dmi.dbis.cs108.client.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.logging.Logger;

/**
 * A UI component that displays a description, typically for a game element like a card.
 * It loads its layout from FXML and styles from CSS.
 */
public class DescriptionDialog extends UIComponent<VBox> {
    private static final Logger LOGGER = Logger.getLogger(DescriptionDialog.class.getName());
    private static final String FXML_PATH = "/fxml/description-dialog.fxml";
    private static final String CSS_PATH = "/css/description-dialog.css";
    private static final double TEXT_PADDING = 30.0;

    @FXML
    private VBox rootVBox;
    
    @FXML
    private Label titleLabel;

    @FXML
    private Text descriptionText;

    /**
     * Creates a new DescriptionDialog component.
     */
    public DescriptionDialog() {
        super(FXML_PATH);
        loadStylesheets();
        setupDynamicWidthBinding();
        // Initially hidden
        getView().setVisible(false);
        getView().setManaged(false);
    }

    private void loadStylesheets() {
        try {
            var cssResource = getClass().getResource(CSS_PATH);
            if (cssResource != null) {
                getView().getStylesheets().add(cssResource.toExternalForm());
            } else {
                LOGGER.warning("Could not find CSS resource: " + CSS_PATH);
            }
        } catch (Exception e) {
            LOGGER.warning("Error loading CSS for DescriptionDialog: " + e.getMessage());
        }
    }
    
    private void setupDynamicWidthBinding() {
        // Bind the text wrapping width to the container width minus padding
        rootVBox.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.doubleValue() > TEXT_PADDING) {
                descriptionText.setWrappingWidth(newVal.doubleValue() - TEXT_PADDING);
            }
        });
    }

    /**
     * Shows the dialog with the given title and description.
     *
     * @param title       The title to display.
     * @param description The description text.
     */
    public void show(String title, String description) {
        titleLabel.setText(title);
        descriptionText.setText(description);
        getView().setVisible(true);
        getView().setManaged(true);
        getView().toFront(); // Ensure it's on top
    }

    /**
     * Hides the dialog.
     */
    public void hide() {
        getView().setVisible(false);
        getView().setManaged(false);
    }
}
