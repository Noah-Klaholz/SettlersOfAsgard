package ch.unibas.dmi.dbis.cs108.server;

import org.junit.jupiter.api.*;
import java.io.IOException;

class ServerTest {

    @Test
    public void testServer() throws IOException {

        System.out.println("Testing");
        // Start server in a new thread
        new Thread(() -> new GameServer(9000).start()).start();

        // Small delay to let the server start properly
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Start client
        GameClient client = new GameClient("127.0.0.1", 9000);
        client.connect();

        // Send a test command
        client.sendMessage("testCommand:arg1,arg2,arg3");

        // Wait a bit to see the response
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Disconnect client
        client.disconnect();

        System.out.println("Test complete.");
        System.exit(0); // Ensure program terminates cleanly
    }
}

