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
import java.util.logging.Logger;

/**
 * The GameServer class is responsible for managing the clients and their connections.
 * It listens for incoming connections and creates a new ClientHandler for each client.
 */
public class GameServer {
    private static final Logger logger = Logger.getLogger(GameServer.class.getName());
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
            logger.info("Server started on port " + port);

            while (running) {
                try {
                    logger.info("Waiting for client connection...");
                    ClientHandler client = new ClientHandler(serverSocket.accept());
                    clients.add(client);
                    executor.execute(client);
                } catch (SocketException se) {
                    if(!running) {
                        logger.info("Server socket closed. Exiting accept loop");
                        break; // Expected exception when server is shutting down
                    } else {
                        throw se;
                    }
                }
            }
        } catch (IOException e) {
            logger.severe("Server error:" + e.getMessage());
        }
    }

    /**
     * Stops the server and closes all connections.
     */
    public void shutdown() {
        running = false;
        // Disconnect all clients
        broadcast("STDN:");
        for (ClientHandler client : clients) {  //forcefully close all sockets and clear the clients list
            try {
                client.closeSocket();
            } catch (IOException e) {
                logger.warning("Error closing client socket: " + e.getMessage());
            }
        }
        clients.clear();
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warning("Executor shutdown interrupted: " + e.getMessage());
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.warning("Error closing server socket: " + e.getMessage());
            }
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
        if (clients.contains(client)) {
            clients.remove(client);
            logger.info("Removed " + client);
        }
    }

    @Override
    public String toString() {
        return "GameServer{" +
                "port=" + port +
                ", running=" + running +
                ", clients=" + clients.size() +
                '}';
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
                socket.setSoTimeout(5000); // 5 second timeout
                out = new PrintWriter(socket.getOutputStream(), true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                logger.severe("Error setting up client handler: " + e.getMessage());
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
                    logger.info("Server received: " + received);
                    processMessage(received);
                }
            } catch (IOException e) {
                logger.info("Client disconnected unexpectedly: " + e.getMessage());
            } finally {
                closeResources();
                removeClient(this);
            }
        }

        /**
         * Checks if the server is running.
         * @return the state of the server
         */
        public boolean isRunning() {
            return running;
        }

        /**
         * Sends a message to the client.
         * @param message The message to send
         */
        @Override
        public void sendMessage(String message) {
            if(socket != null && !socket.isClosed()) {
                out.println(message);
            } else {
                logger.info("Client socket is closed. Unable to send message: " + message);
            }
        }

        /**
         * Receives a message from the client and processes it. Answers with an OK or ERR response depending on success of processing.
         * @param received The message received from the client
         *                 The message should be in the format "commandName:arg1,arg2,arg3"
         */
        @Override
        public void processMessage(String received) {
            if (received == null || received.trim().isEmpty()) {
                logger.warning("Received null or empty message");
                sendMessage("ERR0R:103;Null");
                return;
            }
            Command cmd = new Command(received);
            if (cmd.isValid()) {
                boolean processed = true; // Assume command is processed, only change in default (error) case
                logger.info("Server processing " + cmd);

                switch (cmd.getCommand()) {
                    case NetworkProtocol.TEST:
                        logger.info("TEST");
                        break;
                    case NetworkProtocol.OK:
                        processed = false;
                        break;
                    case NetworkProtocol.ERROR:
                        processed = false;
                        logger.info("Server sent an error command.");
                        break;
                    default: // Error case
                        logger.warning("Unknown command: " + cmd.getCommand());
                        processed = false;
                }
                if(processed) {
                    sendMessage("OK:" + cmd.toString()); // Echo the command back to the client with an OK response
                } else {
                    sendMessage("ERR:" + cmd.toString()); // Echo the command back to the client with an ERR response
                }
            } else {
                logger.warning("Invalid command: " + cmd);
            }
        }

        /**
         * Closes the client socket.
         * @throws IOException if an error occurs while closing the socket
         */
        public void closeSocket() throws IOException {
            closeResources();
        }

        /**
         * Closes the client socket and associated resources.
         */
        private void closeResources() {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                logger.warning("Error closing PrintWriter: " + e.getMessage());
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                logger.warning("Error closing BufferedReader: " + e.getMessage());
            }
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception e) {
                logger.warning("Error closing socket: " + e.getMessage());
            }
        }

        @Override
        public String toString() {
            return "ClientHandler{" +
                    "socket=" + (socket != null ? socket.getRemoteSocketAddress() : "disconnected") +
                    "}";
        }
    }
}
