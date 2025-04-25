package ch.unibas.dmi.dbis.cs108.client.ui.utils;

/**
 * Class representing the details of a card.
 * Contains the title, description, and lore of the card.
 */
public class CardDetails {
    /**Map of card IDs to CardDetails objects.*/
    final String title;
    /**Map of card IDs to CardDetails objects.*/
    final String description;
    /**Map of card IDs to CardDetails objects.*/
    final String lore;

    /**
     * Constructor for CardDetails.
     *
     * @param title       The title of the card.
     * @param description The description of the card.
     * @param lore        The lore of the card.
     */
    public CardDetails(String title, String description, String lore) {
        this.title = title;
        this.description = description;
        this.lore = lore;
    }

    /**
     * Retrieves the title of the card.
     *
     * @return The title of the card.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Retrieves the description of the card.
     *
     * @return The description of the card.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retrieves the lore of the card.
     *
     * @return The lore of the card.
     */
    public String getLore() {
        return lore;
    }
}