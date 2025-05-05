package ch.unibas.dmi.dbis.cs108.client.ui.components.game.tooltips;

import javafx.scene.control.Tooltip;
import javafx.scene.Node;

/**
 * A reusable tooltip for displaying dynamic information about actions.
 */
public class ActionTooltip extends Tooltip {

    public ActionTooltip() {
        super();
        // Apply existing tooltip styles
        getStyleClass().add("action-tooltip");
    }

    public ActionTooltip(String text) {
        super(text);
        getStyleClass().add("action-tooltip");
    }

    /**
     * Updates the tooltip text dynamically.
     *
     * @param text The new text to display.
     */
    public void updateText(String text) {
        setText(text);
    }

    /**
     * Installs the tooltip on a given node.
     *
     * @param node The node to attach the tooltip to.
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
}
