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
    private static final Logger LOGGER = Logger.getLogger(StatueDetailsWrapper.class.getName());
    private static final Map<Integer, StatueDetailsWrapper> CACHE = new HashMap<>();
    private final Statue statue;
    private final ResourceLoader resourceLoader;
    private Image cardImage;
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
    public String getName() {
        return statue.getName();
    }

    public String getDescription() {
        return statue.getDescription();
    }

    public String getUsage() {
        return statue.getUsage();
    }

    public String getDeal() {
        return statue.getDeal();
    }

    public String getBlessing() {
        return statue.getBlessing();
    }

    public String getCurse() {
        return statue.getCurse();
    }

    public int getPrice() {
        return statue.getPrice();
    }

    public int getUpgradePrice() {
        return statue.getUpgradePrice();
    }

    public int getLevel() {
        return statue.getLevel();
    }

    public boolean isActivated() {
        return statue.isActivated();
    }

    public int getId() {
        return statue.getId();
    }

    public Image getCardImage() {
        return cardImage;
    }

    public Image getMapImage() {
        return mapImage;
    }

    public String getWorld() {
        return statue.getWorld();
    }

    public boolean hasDeployableDeal() {
        return statue.getLevel() >= 1 && !statue.getDeal().isEmpty();
    }

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
