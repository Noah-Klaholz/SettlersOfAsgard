package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.client.observer.GameEventListener;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClient {
    private static GameClient instance; // Singleton instance
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private boolean connected;
    private GameEventListener listener;

    private GameClient(String serverAddress, int port) {

    }

    public static GameClient getInstance(String serverAddress, int port) {
        if (instance == null) {
            instance = new GameClient(serverAddress, port);
        }
        return instance;
    }

    public void setListener(GameEventListener listener) {

    }

    public void sendMessage(String message) {

    }

    private void listenForMessages() {

    }

    public void disconnect() {

    }
}
