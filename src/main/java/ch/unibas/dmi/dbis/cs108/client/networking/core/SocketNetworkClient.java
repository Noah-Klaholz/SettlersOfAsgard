package ch.unibas.dmi.dbis.cs108.client.networking.core;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SocketNetworkClient is a network client that uses a socket to communicate with a server.
 * It handles sending and receiving messages asynchronously and provides a message handler for processing incoming messages.
 */
public class SocketNetworkClient implements NetworkClient {
    private static final Logger LOGGER = Logger.getLogger(SocketNetworkClient.class.getName());
    private final ExecutorService executorService;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageHandler messageHandler;
    private volatile boolean running;
    private Thread readerThread;

    /**
     * Constructor for SocketNetworkClient.
     * Initializes the executor service for handling network operations.
     */
    public SocketNetworkClient() {
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "NetworkClientThread");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Constructor for SocketNetworkClient with a custom executor service.
     * This allows for more control over the threading model used by the client.
     *
     * @param host The host address of the server.
     * @param port The port number of the server.
     */
    @Override
    public CompletableFuture<Void> connect(String host, int port) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                socket = new Socket(host, port);
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                running = true;
                startReaderThread();
                future.complete(null);
            } catch (Exception e) {
                cleanupResources();
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private void cleanupResources() {
        try {
            if (readerThread != null) {
                readerThread.interrupt();
            }
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to close socket", e);
        }
    }

    /**
     * Starts a thread to read messages from the socket.
     * This method is called when the client successfully connects to the server.
     */
    private void startReaderThread() {
        readerThread = new Thread(() -> {
            try {
                String line;
                while (running && (line = in.readLine()) != null) {
                    final String message = line;
                    executorService.submit(() -> {
                        if (messageHandler != null) {
                            messageHandler.onMessage(message);
                        }
                    });
                }
            } catch (IOException e) {
                if (running) {
                    executorService.submit(() -> {
                        if (messageHandler != null) {
                            messageHandler.onDisconnect(e);
                        }
                    });
                }
            } finally {
                cleanupResources();
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    /**
     * Disconnects from the server and cleans up resources.
     * This method is called when the client is no longer needed or when an error occurs.
     */
    @Override
    public void disconnect() {
        if (!running) return;
        running = false;
        cleanupResources();
    }

    /**
     * Sends a message to the server.
     * This method is asynchronous and returns a CompletableFuture.
     *
     * @param message The message to send.
     * @return A CompletableFuture that completes when the message is sent.
     */
    @Override
    public CompletableFuture<Void> send(String message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        executorService.submit(() -> {
            if (!isConnected()) {
                future.completeExceptionally(new IOException("Not connected"));
                return;
            }
            out.println(message);
            if (out.checkError()) {
                future.completeExceptionally(new IOException("Failed to send message"));
                notifyDisconnect(new IOException("Connection lost"));
            } else {
                future.complete(null);
            }
        });
        return future;
    }

    /**
     * Checks if the client is connected to the server.
     *
     * @return true if connected, false otherwise.
     */
    @Override
    public boolean isConnected() {
        return running && socket != null && !socket.isClosed();
    }

    /**
     * Sets the message handler for processing incoming messages.
     * This allows the client to handle messages in a custom way.
     *
     * @param handler The message handler to set.
     */
    @Override
    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }

    /**
     * Notifies about the disconnect
     *
     * @param cause the cause if the disconnect
     */
    private void notifyDisconnect(Throwable cause) {
        if (!running) return;
        running = false;
        if (messageHandler != null) {
            executorService.submit(() -> messageHandler.onDisconnect(cause));
        }
        disconnect();
    }
}