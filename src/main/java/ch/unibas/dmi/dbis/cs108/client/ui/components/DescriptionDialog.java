package ch.unibas.dmi.dbis.cs108.client.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.logging.Logger;

/**
 * A UI component that displays a title and a description, typically for a game
 * element.
 * Loads its layout from FXML and styles from CSS.
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
        setupDynamicWidthBinding();
        // Initial state is invisible - the controller will handle visibility
        getView().setVisible(false);
        getView().setManaged(false);
    }

    /**
     * Sets up dynamic width binding for the description text.
     */
    private void setupDynamicWidthBinding() {
        rootVBox.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.doubleValue() > TEXT_PADDING) {
                descriptionText.setWrappingWidth(newVal.doubleValue() - TEXT_PADDING);
            }
        });
    }

    /**
     * Updates dialog content without changing visibility.
     * Use this when the controller manages visibility and animations.
     *
     * @param title       The title to display.
     * @param description The description text.
     */
    public void setContent(String title, String description) {
        if (titleLabel == null || descriptionText == null) {
            LOGGER.severe("Cannot update content: FXML elements not injected correctly.");
            return;
        }
        titleLabel.setText(title);
        descriptionText.setText(description);
    }

    /**
     * Makes the dialog visible. This is typically called by the BaseController
     * or when the dialog is not managed as an overlay.
     */
    @Override
    public void show() {
        getView().setVisible(true);
        getView().setManaged(true);
        getView().toFront();
    }

    /**
     * Shows the dialog with the given title and description.
     * Sets content and makes the view visible.
     *
     * @param title       The title to display.
     * @param description The description text.
     * @deprecated Use setContent() and show() separately, or use BaseController's
     *             overlay management.
     */
    @Deprecated
    public void show(String title, String description) {
        setContent(title, description);
        show(); // Call the new show() method
    }

    /**
     * Hides the dialog and calls the onCloseAction if set.
     * Renamed from hide() for consistency.
     */
    public void close() {
        getView().setVisible(false);
        getView().setManaged(false);
        // Call the onCloseAction if it's set
        Runnable action = getOnCloseAction();
        if (action != null) {
            action.run();
        }
    }
}
