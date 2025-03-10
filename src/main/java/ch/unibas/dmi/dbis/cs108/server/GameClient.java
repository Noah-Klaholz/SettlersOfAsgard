package ch.unibas.dmi.dbis.cs108.server;

import java.io.*;
import java.net.*;

/**
 * The GameClient class is responsible for connecting to the server and sending/receiving messages.
 */
public class GameClient implements CommunicationAPI {
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
                String received;
                try {
                    while ((received = in.readLine()) != null) {
                        System.out.println("Client received message: " + received);
                        processMessage(received);
                    }
                } catch (SocketException se) {
                    System.out.println("Socket closed, exiting reading thread"); // Expected during shutdown
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
        System.out.println("Client Disconnected from port " + port);
    }

    /**
     * Starts the client and sends a test message to the server.
     */
    public void start() {
        //TODO implement client logic (Actual Game Logic) -> Game Should start here (call to main menu)
        sendMessage(NetworkProtocol.TEST+":arg1,arg2,arg3");    // test command
    }

    /**
     * Sends a message to the server.
     * @param message The message to send
     */
    @Override
    public void sendMessage(String message) {
        out.println(message);
    }

    /**
     * Receives a message from the server and processes it. Answers with an OK or ERR response depending on success of processing.
     * @param received The received message from the server
     *                 Message String should be in the format "commandName:arg1,arg2,arg3"
     */
    @Override
    public void processMessage(String received) {
        Command cmd = new Command(received);
        if (cmd.isValid()) {
            System.out.println("Client processing " + cmd);

            switch (cmd.getCommand()) {
                case NetworkProtocol.TEST:
                    System.out.println("TEST");
                    break;
                case NetworkProtocol.SHUTDOWN:
                    System.out.println("Server sent a shutdown command. Disconnecting...");
                    disconnect();
                case NetworkProtocol.OK:
                    break;
                case NetworkProtocol.ERROR:
                    System.out.println("Server sent an error command.");
                    break;
                default:
                    System.err.println("Unknown command: " + cmd.getCommand());
            }
        } else {
            System.err.println("Invalid command: " + cmd);
        }
    }
}
