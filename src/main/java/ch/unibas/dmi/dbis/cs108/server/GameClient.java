package ch.unibas.dmi.dbis.cs108.server;

import java.io.*;
import java.net.*;

/**
 * The GameClient class is responsible for connecting to the server and sending/receiving messages.
 */
public class GameClient implements CommunicationAPI{
    private String host;
    private int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;


    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Connects to the server.
     */
    public void connect() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to server on port " + port);

            new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        System.out.println("Server sent message: " + message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public String receiveMessage() {
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        GameClient client = new GameClient("localhost", 12345);
        client.connect();
        // Send a test command. For example: MOVE:UP (following the protocol "COMMAND:args")
        client.sendMessage("MOVE:UP");
    }
}
