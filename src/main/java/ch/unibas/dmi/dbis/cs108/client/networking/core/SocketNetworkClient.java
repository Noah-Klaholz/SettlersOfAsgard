package ch.unibas.dmi.dbis.cs108.client.networking.core;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketNetworkClient implements NetworkClient {
    private static final Logger LOGGER = Logger.getLogger(SocketNetworkClient.class.getName());
    private final ExecutorService executorService;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageHandler messageHandler;
    private volatile boolean running;
    private Thread readerThread;

    public SocketNetworkClient() {
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "NetworkClientThread");
            thread.setDaemon(true);
            return thread;
        });
    }

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
                LOGGER.info("Connected to " + host + ":" + port);
            } catch (Exception e) {
                // Ensure the socket is closed if a connection error occurs.
                if (socket != null && !socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (IOException ioEx) {
                        LOGGER.log(Level.WARNING, "Error closing socket after connection failure", ioEx);
                    }
                }
                future.completeExceptionally(e);
                LOGGER.log(Level.SEVERE, "Failed to connect", e);
            }
        });
        return future;
    }

    private void startReaderThread() {
        readerThread = new Thread(() -> {
            try {
                String line;
                while (running && (line = in.readLine()) != null) {
                    final String message = line;
                    if (messageHandler != null) {
                        executorService.submit(() -> messageHandler.onMessage(message));
                    }
                }
            } catch (IOException e) {
                if (running) {
                    notifyDisconnect(e);
                }
            } finally {
                notifyDisconnect(new IOException("Stream closed"));
            }
        }, "SocketReaderThread");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    @Override
    public void disconnect() {
        running = false;
        try {
            if (readerThread != null) {
                readerThread.interrupt();
            }
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            LOGGER.info("Disconnected from server");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error during disconnect", e);
        }
    }

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

    @Override
    public boolean isConnected() {
        return running && socket != null && !socket.isClosed();
    }

    @Override
    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }

    private void notifyDisconnect(Throwable cause) {
        if (!running) return;
        running = false;
        if (messageHandler != null) {
            executorService.submit(() -> messageHandler.onDisconnect(cause));
        }
        disconnect();
    }
}