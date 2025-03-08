package ch.unibas.dmi.dbis.cs108.server;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

public class GameServer {
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService executor;

    public GameServer(int port) {
        this.port = port;
    }

    public void start() {}
}


