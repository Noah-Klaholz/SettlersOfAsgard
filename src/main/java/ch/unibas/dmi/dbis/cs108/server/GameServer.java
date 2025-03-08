package ch.unibas.dmi.dbis.cs108.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The GameServer class is responsible for managing the clients and their connections.
 * It listens for incoming connections and creates a new ClientHandler for each client.
 */
public class GameServer {
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService executor;
    private List<ClientHandler> clients;

    public GameServer(int port) {
        this.port = port;
        clients = new CopyOnWriteArrayList<>();
        executor = Executors.newCachedThreadPool();
    }

    /**
     * Starts the server and listens for incoming connections.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                System.out.println("Waiting for client connection...");
                ClientHandler client = new ClientHandler(serverSocket.accept());
                clients.add(client);
                executor.execute(client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     * @param message The message to broadcast
     */
    public void broadcast(String message) {
        for(ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    /**
     * Inner class representing a client handler.
     * The ClientHandler class is responsible for handling communication with a single client.
     * It implements the CommunicationAPI interface to send and receive messages.
     */
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
                    if (cmd.isValid()) {
                        processCommand(cmd);
                    } else {
                        System.err.println("Invalid command received: " + received);
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
                removeClient(this);
            }
        }

        /**
         * Process the parsed command.
         * Extend this method to handle different commands (e.g., MOVE, ATTACK, CHAT, etc.)
         */
        private void processCommand(Command cmd) {
            //TODO implement this Method with switch case (Network protocol)
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
}


