package ch.unibas.dmi.dbis.cs108.client.networking;
import java.io.InputStreamReader;
import java.net.Socket;
import ch.unibas.dmi.dbis.cs108.client.core.observer.GameEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketHandler {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;


    public SocketHandler(String serverAddress, int serverPort) throws IOException {

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

    }


}