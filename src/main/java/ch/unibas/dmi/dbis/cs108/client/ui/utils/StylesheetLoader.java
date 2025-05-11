package ch.unibas.dmi.dbis.cs108.client.ui.utils;

import javafx.scene.Parent;

import java.net.URL;
import java.util.logging.Logger;

/**
 * Utility class for loading stylesheets in a consistent order across
 * components.
 */
public class StylesheetLoader {
    // Stylesheet paths
    /**
     * The path to the CSS file containing the variables.
     */
    public static final String VARIABLES_CSS = "/css/variables.css";
    /**
     * The path to the CSS file containing common styles.
     */
    public static final String COMMON_CSS = "/css/common.css";
    /**
     * The path to the CSS file containing dialog common styles.
     */
    public static final String DIALOG_COMMON_CSS = "/css/dialog-common.css";
    /**
     * The path to the CSS file containing dialog styles.
     */
    private static final Logger LOGGER = Logger.getLogger(StylesheetLoader.class.getName());


    /**
     * Loads core stylesheets (variables + common) for all components.
     * Renamed from loadCommonStylesheets for clarity.
     *
     * @param parent The parent node to apply stylesheets to
     */
    public static void loadCoreStylesheets(Parent parent) {
        loadStylesheet(parent, VARIABLES_CSS);
        loadStylesheet(parent, COMMON_CSS);
    }

    /**
     * Loads stylesheets for dialog components (variables + common + dialog-common)
     *
     * @param parent The parent node to apply stylesheets to
     */
    public static void loadDialogStylesheets(Parent parent) {
        loadStylesheet(parent, VARIABLES_CSS);
        loadStylesheet(parent, COMMON_CSS);
        loadStylesheet(parent, DIALOG_COMMON_CSS);
    }

    /**
     * Loads all specified stylesheets in order
     *
     * @param parent          The parent node to apply stylesheets to
     * @param stylesheetPaths Array of stylesheet paths to load
     */
    public static void loadStylesheets(Parent parent, String... stylesheetPaths) {
        for (String path : stylesheetPaths) {
            loadStylesheet(parent, path);
        }
    }

    /**
     * Helper to load a single stylesheet safely
     *
     * @param parent The parent node to apply the stylesheet to
     * @param path   The path to the stylesheet
     */
    public static void loadStylesheet(Parent parent, String path) {
        try {
            URL cssResource = StylesheetLoader.class.getResource(path);
            if (cssResource != null) {
                String cssPath = cssResource.toExternalForm();
                // Avoid duplicate stylesheets
                if (!parent.getStylesheets().contains(cssPath)) {
                    parent.getStylesheets().add(cssPath);
                }
            } else {
                LOGGER.warning("Could not find CSS resource: " + path);
            }
        } catch (Exception e) {
            LOGGER.warning("Error loading CSS " + path + ": " + e.getMessage());
        }
    }
}
