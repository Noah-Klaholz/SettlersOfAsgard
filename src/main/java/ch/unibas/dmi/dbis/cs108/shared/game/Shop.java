package ch.unibas.dmi.dbis.cs108.shared.game;

import ch.unibas.dmi.dbis.cs108.shared.entities.EntityRegistry;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Statues.Statue;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.Structure;

import java.util.ArrayList;
import java.util.List;

/**
 * Shop class for purchasing game entities.
 * Manages the availability of structures and statues for purchase.
 */
public class Shop {
    /**
     * Flag indicating whether a statue is currently in use.
     * Only one statue can be in use at a time.
     */
    private boolean statueInUse;

    /**
     * Constructs a new Shop instance.
     * The EntityRegistry is statically initialized.
     */
    public Shop() {
        // EntityRegistry is statically initialized
    }

    /**
     * Gets all structures available for purchase.
     *
     * @return List of available structures
     */
    public List<Structure> getBuyableStructures() {
        return new ArrayList<>(EntityRegistry.getAllStructures());
    }

    /**
     * Gets all statues available for purchase.
     * If a statue is already in use, returns an empty list.
     *
     * @return List of available statues, or empty list if one is in use
     */
    public List<Statue> getBuyableStatues() {
        if (statueInUse) {
            return new ArrayList<>(); // No statues available if one is in use
        }
        return new ArrayList<>(EntityRegistry.getAllStatues());
    }

    /**
     * Marks that a statue is currently in use.
     * This prevents other statues from being purchased.
     */
    public void blockStatue() {
        statueInUse = true;
    }
}