package ch.unibas.dmi.dbis.cs108.client.ui.utils;

import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Wrapper class for statue details to be used by UI components.
 * Provides a clean interface to statue data without direct coupling to the
 * entity package.
 */
public class StatueDetailsWrapper {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(StatueDetailsWrapper.class.getName());
    /**
     * Cache for statue details wrappers to avoid redundant object creation.
     */
    private static final Map<Integer, StatueDetailsWrapper> CACHE = new HashMap<>();
    /**
     * The statue entity being wrapped.
     */
    private final Statue statue;
    /**
     * The resource loader for loading images.
     */
    private final ResourceLoader resourceLoader;
    /**
     * The image representing the statue on the card.
     */
    private Image cardImage;
    /**
     * The image representing the statue on the map.
     */
    private Image mapImage;

    /**
     * Creates a new wrapper for the given statue entity.
     *
     * @param statue         The statue entity to wrap
     * @param resourceLoader The resource loader for loading images
     */
    private StatueDetailsWrapper(Statue statue, ResourceLoader resourceLoader) {
        this.statue = statue;
        this.resourceLoader = resourceLoader;
        loadImages();
    }

    /**
     * Gets or creates a wrapper for a statue identified by its ID.
     *
     * @param statueId       The ID of the statue
     * @param resourceLoader The resource loader for loading images
     * @return A wrapper for the statue, or null if the statue does not exist
     */
    public static StatueDetailsWrapper forStatue(int statueId, ResourceLoader resourceLoader) {
        if (CACHE.containsKey(statueId)) {
            return CACHE.get(statueId);
        }

        Statue statue = EntityRegistry.getStatue(statueId);
        if (statue == null) {
            LOGGER.warning("No statue found with ID " + statueId);
            return null;
        }

        StatueDetailsWrapper wrapper = new StatueDetailsWrapper(statue, resourceLoader);
        CACHE.put(statueId, wrapper);
        return wrapper;
    }

    /**
     * Gets or creates a wrapper for the given statue entity.
     *
     * @param statue         The statue entity to wrap
     * @param resourceLoader The resource loader for loading images
     * @return A wrapper for the statue
     */
    public static StatueDetailsWrapper forStatue(Statue statue, ResourceLoader resourceLoader) {
        if (statue == null) {
            LOGGER.warning("Cannot create wrapper for null statue");
            return null;
        }

        int statueId = statue.getId();
        if (CACHE.containsKey(statueId)) {
            return CACHE.get(statueId);
        }

        StatueDetailsWrapper wrapper = new StatueDetailsWrapper(statue, resourceLoader);
        CACHE.put(statueId, wrapper);
        return wrapper;
    }

    /**
     * Loads the images for the statue.
     * This method is called during the construction of the wrapper.
     */
    private void loadImages() {
        if (statue == null)
            return;

        // Load card image
        String cardImagePath = statue.getCardImagePath();
        if (cardImagePath != null && !cardImagePath.isEmpty()) {
            cardImage = resourceLoader.loadImage(cardImagePath);
        }

        // Load map image
        String mapImagePath = statue.getMapImagePath();
        if (mapImagePath != null && !mapImagePath.isEmpty()) {
            mapImage = resourceLoader.loadImage(mapImagePath);
        }
    }

    // Accessor methods for statue properties
    /**
     * Gets the name of the statue.
     *
     * @return The name of the statue
     */
    public String getName() {
        return statue.getName();
    }

    /**
     * Gets the description of the statue.
     *
     * @return The description of the statue
     */
    public String getDescription() {
        return statue.getDescription();
    }

    /**
     * Gets the usage of the statue.
     *
     * @return The usage of the statue
     */
    public String getUsage() {
        return statue.getUsage();
    }

    /**
     * Gets the type of the statue.
     *
     * @return The type of the statue
     */
    public String getDeal() {
        return statue.getDeal();
    }

    /**
     * Gets the blessing of the statue.
     *
     * @return The blessing of the statue
     */
    public String getBlessing() {
        return statue.getBlessing();
    }

    /**
     * Gets the curse of the statue.
     *
     * @return The curse of the statue
     */
    public String getCurse() {
        return statue.getCurse();
    }

    /**
     * Gets the price of the statue.
     *
     * @return The price of the statue
     */
    public int getPrice() {
        return statue.getPrice();
    }

    /**
     * Gets the upgrade price of the statue.
     *
     * @return The upgrade price of the statue
     */
    public int getUpgradePrice() {
        return statue.getUpgradePrice();
    }

    /**
     * Gets the level of the statue.
     *
     * @return The level of the statue
     */
    public int getLevel() {
        return statue.getLevel();
    }

    /**
     * Gets the maximum level of the statue.
     *
     * @return The maximum level of the statue
     */
    public boolean isActivated() {
        return statue.isActivated();
    }

    /**
     * Gets the ID of the statue.
     *
     * @return The ID of the statue
     */
    public int getId() {
        return statue.getId();
    }

    /**
     * Gets the image representing the statue on the card.
     *
     * @return The card image of the statue
     */
    public Image getCardImage() {
        return cardImage;
    }

    /**
     * Gets the image representing the statue on the map.
     *
     * @return The map image of the statue
     */
    public Image getMapImage() {
        return mapImage;
    }

    /**
     * Gets the x-coordinate of the statue.
     *
     * @return The x-coordinate of the statue
     */
    public String getWorld() {
        return statue.getWorld();
    }

    /**
     * Checks if the statue has a deployable deal based on its level.
     *
     * @return true if the statue is level 1 and has a deal
     */
    public boolean hasDeployableDeal() {
        return statue.getLevel() >= 1 && !statue.getDeal().isEmpty();
    }

    /**
     * Checks if the statue has a deployable blessing based on its level.
     *
     * @return true if the statue is level 2 and has a blessing
     */
    public boolean hasDeployableBlessing() {
        return statue.getLevel() >= 2 && !statue.getBlessing().isEmpty();
    }

    /**
     * Checks if the statue has a deployable curse based on its level.
     *
     * @return true if the statue is level 3 and has a curse
     */
    public boolean hasDeployableCurse() {
        return statue.getLevel() >= 3 && !statue.getCurse().isEmpty();
    }

    /**
     * Checks if this is the Freyr statue, which has special tree growing abilities.
     *
     * @return true if this is Freyr (ID 31)
     */
    public boolean isFreyr() {
        return statue.getId() == 31;
    }

    /**
     * Checks if the statue can be interacted with (not disabled).
     *
     * @return true if the statue is not disabled
     */
    public boolean isInteractable() {
        return !statue.isDisabled();
    }

    /**
     * Gets the underlying statue entity.
     * Only use this when absolutely necessary to interact with game logic.
     */
    public Statue getStatue() {
        return statue;
    }
}
