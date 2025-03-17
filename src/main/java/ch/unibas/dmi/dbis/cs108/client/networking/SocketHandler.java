package ch.unibas.dmi.dbis.cs108.client.networking;
import java.io.InputStreamReader;
import java.net.Socket;
import ch.unibas.dmi.dbis.cs108.client.observer.GameEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketHandler {
    private static SocketHandler instance;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private GameEventListener listener;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    private SocketHandler(String serverAddress, int serverPort) throws IOException {


    }


    public static synchronized SocketHandler getInstance(String serverAddress, int serverPort) throws IOException {

        return instance;
    }


    public void setListener(GameEventListener listener) {

    }

    // Send message to the server
    public void sendMessage(String message) {

    }

    // Start listening for incoming data
    private void startListening() {

    }

    public void closeConnection() {

    }

}
