package ch.unibas.dmi.dbis.cs108.client.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * SocketHandler class is responsible for handling the socket connection
 */
public class SocketHandler {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private boolean connected = false;

    /**
     * Constructor
     *
     * @param host String
     * @param port int
     * @throws IOException
     */
    public SocketHandler(String host, int port) throws IOException {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            connected = true;
        } catch (IOException e) {
            connected = false;
            throw e;
        }
    }

    /**
     * Checks if the socket is connected
     *
     * @return boolean
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    /**
     * Sends a message through the socket
     *
     * @param message String
     */
    public void send(String message) {
        if (out != null && isConnected()) {
            out.println(message);
            if (out.checkError()) {
                connected = false;
            }
        }
    }

    /**
     * Receives a message from the socket
     *
     * @return String
     * @throws IOException
     */
    public String receive() throws IOException {
        if (in != null && isConnected()) {
            try {
                if (in.ready()) { // Only check ready() to avoid blocking
                    String message = in.readLine();
                    return message;
                }
            } catch (SocketException e) {
                connected = false;
                throw e;
            }
        }
        return null;
    }

    /**
     * Closes the socket connection
     */
    public void close() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                connected = false;
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

}