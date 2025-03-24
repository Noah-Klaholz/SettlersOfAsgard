package ch.unibas.dmi.dbis.cs108.client.core.observer;

/**
 * interface handling user inputs
 */
public interface GameEventListener {
    void onMessageReceived(String serverMessage);
}
