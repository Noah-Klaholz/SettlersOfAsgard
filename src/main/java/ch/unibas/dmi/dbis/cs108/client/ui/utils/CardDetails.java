package ch.unibas.dmi.dbis.cs108.client.ui.utils;

import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.GameEntity;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;

/**
 * Class representing the details of a card.
 * Contains the title, description, and lore of the card.
 */
public class CardDetails {
    /**
     * The ID of the card.
     */
    final int id;
    /**
     * Map of card IDs to CardDetails objects.
     */
    final String title;
    /**
     * Map of card IDs to CardDetails objects.
     */
    final String description;
    /**
     * Map of card IDs to CardDetails objects.
     */
    final String lore;
    /**
     * URL for the image associated with the card.
     */
    final String imageUrl;
    /**
     * Price of the card in runes.
     */
    final int price;
    /**
     * The entity that is represented by this Card
     */
    final GameEntity entity;

    /**
     * Constructor for CardDetails.
     *
     * @param id            The ID of the card.
     * @param title         The title of the card.
     * @param description   The description of the card.
     * @param lore          The lore of the card.
     * @param cardImagePath The path to the card image.
     * @param price         The price of the card in runes.
     */
    public CardDetails(int id, String title, String description, String lore, String cardImagePath, int price) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.lore = lore;
        this.imageUrl = cardImagePath;
        this.price = price;
        this.entity = EntityRegistry.getGameEntityOriginalById(id);
    }

    /**
     * Constructor for CardDetails using a GameEntity.
     *
     * @param gameEntity The GameEntity object representing the card.
     * @param isCard     Indicates if the entity is a card.
     */
    public CardDetails(GameEntity gameEntity, boolean isCard) {
        this.id = gameEntity.getId();
        this.title = gameEntity.getName();
        this.description = gameEntity.getDescription();
        this.lore = gameEntity.getUsage();
        this.imageUrl = EntityRegistry.getURL(id, isCard);
        this.price = gameEntity.getPrice();
        this.entity = gameEntity;
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

    /**
     * Retrieves the image URL of the card.
     *
     * @return The image URL of the card.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Retrieves the price of the card.
     *
     * @return The price of the card.
     */
    public int getPrice() {
        return price;
    }

    /**
     * Retrieves the id of the entity for the card
     *
     * @return the id
     */
    public int getID() {
        return id;
    }

    /**
     * Retrieves the GameEntity object represented by this CardDetails
     *
     * @return the entity
     */
    public GameEntity getEntity() {
        return entity;
    }
}