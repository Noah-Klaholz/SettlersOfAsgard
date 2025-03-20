package ch.unibas.dmi.dbis.cs108.client.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class SocketHandler {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;


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

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    public void send(String message){
        if (out != null && isConnected()) {
            out.println(message);
            if (out.checkError()) {
                connected = false;
            }
        }
    }

    public String receive() throws IOException{
        if (in != null && isConnected() && in.ready()) {
            try {
                String message = in.readLine();
                return message;
            } catch (SocketException e) {
                connected = false;
                throw e;
            }
        }
        return null;
    }

    public void close(){
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