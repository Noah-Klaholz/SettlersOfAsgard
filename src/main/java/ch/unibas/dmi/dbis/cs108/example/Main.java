package ch.unibas.dmi.dbis.cs108.example;

import ch.unibas.dmi.dbis.cs108.server.*;
/**
 * testing the HexMap and HexTiles classes
 */
public class Main {
    public static void main(String[] args){
        // Start server in a new thread
        String[] serverArgs = {"server", "9000"};
        ServerMain.main(serverArgs);

        // Small delay to let the server start properly
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Start client
        String[] clientArgs = {"client", "127.0.0.1:9000"};
        ServerMain.main(clientArgs);

        // Wait a bit to see the response
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        System.exit(0); // Ensure program terminates cleanly
    }
}
