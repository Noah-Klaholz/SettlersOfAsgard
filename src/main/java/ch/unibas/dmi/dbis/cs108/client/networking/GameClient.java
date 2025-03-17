package ch.unibas.dmi.dbis.cs108.client.networking;

import ch.unibas.dmi.dbis.cs108.client.observer.GameEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        try {
            socket = new Socket(serverAddress, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            System.out.println("[Client] Connected to server at " + serverAddress + ":" + port);

            new Thread(this::listenForMessages).start();
        } catch (IOException e) {
            System.err.println("[Client] Connection failed: " + e.getMessage());
        }
    }

    public static GameClient getInstance(String serverAddress, int port) {
        if (instance == null) {
            instance = new GameClient(serverAddress, port);
        }
        return instance;
    }

    public void setListener(GameEventListener listener) {
        this.listener = listener;
    }

    public void sendMessage(String message) {

    }

    private void listenForMessages() {

    }

    public void disconnect() {

    }
}
