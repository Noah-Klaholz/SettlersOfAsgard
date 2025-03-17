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

        try{
            this.socket = new Socket(serverAddress, serverPort);
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("[SocketHandler] Connected to server at " + serverAddress + ":" + serverPort);

            startListening();
        } catch (IOException e) {
            System.err.println("[SocketHandler] Connection failed: " + e.getMessage());
            throw e;
        }
    }


    public static synchronized SocketHandler getInstance(String serverAddress, int serverPort) throws IOException {

        if (instance == null) {
            instance = new SocketHandler(serverAddress, serverPort);
        }
        return instance;
    }


    public void setListener(GameEventListener listener) {

        this.listener = listener;

    }


    public void sendMessage(String message) {

        if (output != null) {
            output.println(message);
            System.out.println("[SocketHandler] Sent: " + message);
        } else {
            System.err.println("[SocketHandler] Output stream is not available.");
        }

    }

    private void startListening() {


    }

    public void closeConnection() {



    }

}
