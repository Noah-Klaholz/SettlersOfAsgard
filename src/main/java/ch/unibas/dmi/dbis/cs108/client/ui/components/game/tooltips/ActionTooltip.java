package ch.unibas.dmi.dbis.cs108.client.ui.components.game.tooltips;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;

/**
 * A reusable tooltip for displaying dynamic information about actions.
 */
public class ActionTooltip extends Tooltip {

    /**
     * Default constructor for ActionTooltip.
     * Initializes the tooltip with default styles.
     */
    public ActionTooltip() {
        super();
        // Apply existing tooltip styles
        getStyleClass().add("action-tooltip");
    }

    /**
     * Constructor for ActionTooltip with custom text.
     *
     * @param text The text to display in the tooltip.
     */
    public ActionTooltip(String text) {
        super(text);
        getStyleClass().add("action-tooltip");
    }

    /**
     * Installs the tooltip on a given node.
     *
     * @param node The node to attach the tooltip to.
     * @param tooltip The tooltip to install.
     */
    public static void install(Node node, ActionTooltip tooltip) {
        Tooltip.install(node, tooltip);
    }

    /**
     * Creates and installs a tooltip with static text.
     *
     * @param node The node.
     * @param text The static text.
     */
    public static void install(Node node, String text) {
        Tooltip.install(node, new ActionTooltip(text));
    }

    /**
     * Updates the tooltip text dynamically.
     *
     * @param text The new text to display.
     */
    public void updateText(String text) {
        setText(text);
    }
}
