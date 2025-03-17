package ch.unibas.dmi.dbis.cs108.client.observer;

public interface GameEventListener {
    void onMessageReceived(String serverMessage);
}
