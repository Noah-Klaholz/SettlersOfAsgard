package ch.unibas.dmi.dbis.cs108.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The GameServer class is responsible for managing the clients and their connections.
 * It listens for incoming connections and creates a new ClientHandler for each client.
 */
public class GameServer {
    private boolean running;
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
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (running) {
                try {
                    System.out.println("Waiting for client connection...");
                    ClientHandler client = new ClientHandler(serverSocket.accept());
                    clients.add(client);
                    executor.execute(client);
                } catch (SocketException se) {
                    if(!running) {
                        System.out.println("Server socket closed. Exiting accept loop");
                        break; // Expected exception when server is shutting down
                    } else {
                        throw se;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the server and closes all connections.
     */
    public void shutdown() {
        running = false;
        // Disconnect all clients
        broadcast("STDN:");
        try {
            if(executor != null) {
                executor.shutdown();
                if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            executor.shutdownNow();
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

    /**
     * Removes a client from the list of connected clients.
     * @param client The client to remove
     */
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
         * Closes the connection when the client disconnects.
         */
        @Override
        public void run() {
            String received;
            try {
                while ((received = in.readLine()) != null) {
                    System.out.println("Server received: " + received);
                    processMessage(received);
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
         * Sends a message to the client.
         * @param message The message to send
         */
        @Override
        public void sendMessage(String message) {
            out.println(message);
        }

        /**
         * Receives a message from the client and processes it. Answers with an OK or ERR response depending on success of processing.
         * @param received The message received from the client
         *                 The message should be in the format "commandName:arg1,arg2,arg3"
         */
        @Override
        public void processMessage(String received) {
            Command cmd = new Command(received);
            if (cmd.isValid()) {
                boolean processed = true; // Assume command is processed, only change in default (error) case
                System.out.println("Client processing " + cmd);

                switch (cmd.getCommand()) {
                    case NetworkProtocol.TEST:
                        System.out.println("TEST");
                        break;
                    default: // Error case
                        System.err.println("Unknown command: " + cmd.getCommand());
                        processed = false;
                }
                if(processed) {
                    sendMessage("OK:" + cmd.toString()); // Echo the command back to the client with an OK response
                } else {
                    sendMessage("ERR:" + cmd.toString()); // Echo the command back to the client with an ERR response
                }
            } else {
                System.err.println("Invalid command: " + cmd);
            }
        }
    }
}


