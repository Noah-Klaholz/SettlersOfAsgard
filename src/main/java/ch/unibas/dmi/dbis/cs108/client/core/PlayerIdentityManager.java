package ch.unibas.dmi.dbis.cs108.client.core;

import ch.unibas.dmi.dbis.cs108.shared.game.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Manages player identity throughout the application.
 * Ensures consistent player information across all components.
 */
public class PlayerIdentityManager {
    /**
     * Logger for PlayerIdentityManager.
     */
    private static final Logger LOGGER = Logger.getLogger(PlayerIdentityManager.class.getName());
    /**
     * Singleton instance of PlayerIdentityManager.
     */
    private static final PlayerIdentityManager INSTANCE = new PlayerIdentityManager();
    /**
     * List of listeners to notify on player updates.
     */
    private final List<Consumer<Player>> listeners = new CopyOnWriteArrayList<>();
    /**
     * Default player name if not set.
     */
    private Player localPlayer;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the local player with a default name.
     */
    private PlayerIdentityManager() {
        this.localPlayer = new Player("Guest");
        LOGGER.info("PlayerIdentityManager initialized with player: " + localPlayer.getName());
    }

    /**
     * Returns the singleton instance of PlayerIdentityManager.
     *
     * @return the singleton instance
     */
    public static PlayerIdentityManager getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the local player.
     *
     * @return the local player
     */
    public Player getLocalPlayer() {
        return localPlayer;
    }

    /**
     * Sets the local player.
     *
     * @param player the new local player
     */
    public void setLocalPlayer(Player player) {
        if (player == null) {
            LOGGER.severe("Attempted to set null player");
            return;
        }

        this.localPlayer = player;
        LOGGER.info("Local player set to: " + player.getName());
        notifyListeners();
    }

    /**
     * Returns the name of the local player.
     *
     * @param newName the name of the local player
     */
    public void updatePlayerName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            LOGGER.warning("Attempted to set empty player name");
            return;
        }

        LOGGER.info("Updating player name from '" + localPlayer.getName() + "' to '" + newName + "'");
        localPlayer.setName(newName);
        notifyListeners();
    }

    /**
     * Sets the local player to a guest with a default name.
     *
     * @param listener the listener to notify
     */
    public void addPlayerUpdateListener(Consumer<Player> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener from the list of listeners.
     *
     * @param listener the listener to remove
     */
    public void removePlayerUpdateListener(Consumer<Player> listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners of a player update.
     */
    private void notifyListeners() {
        for (Consumer<Player> listener : listeners) {
            try {
                listener.accept(localPlayer);
            } catch (Exception e) {
                LOGGER.warning("Error notifying listener: " + e.getMessage());
            }
        }
    }
}