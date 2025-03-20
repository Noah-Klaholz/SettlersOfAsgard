package ch.unibas.dmi.dbis.cs108.client.core.observer;

public interface GameEventListener {
    void onMessageReceived(String serverMessage);
}
