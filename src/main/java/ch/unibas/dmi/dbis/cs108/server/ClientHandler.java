package ch.unibas.dmi.dbis.cs108.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable, CommunicationAPI {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Continuously reads messages from the client and processes them.
     */
    @Override
    public void run() {
        String received;
        try {
            while ((received = in.readLine()) != null) {
                System.out.println("Received: " + received);
                // Use the message parser to extract command info.
                Command cmd = new Command(received);
                if (cmd != null) {
                    processCommand(cmd);
                } else {
                    System.out.println("Invalid message format received: " + received);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //TODO Implement: removeClient(this)
            //removeClient(this);
        }
    }

    /**
     * Process the parsed command.
     * Extend this method to handle different commands (e.g., MOVE, ATTACK, CHAT, etc.)
     */
    private void processCommand(Command cmd) {
        System.out.println("Processing " + cmd);
        // Example: If the command is a move, you might update the player's position.
        // You can also add logic for synchronous response if needed.
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
}

