package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.server.networking.GameServer;
import ch.unibas.dmi.dbis.cs108.server.old.GameClient;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This class tests the ping functionality of the server.
 */
class PingTest {

    private GameServer server;
    private Thread serverThread;

    /**
     * Set up the server before each test.
     */
    @BeforeEach
    public void setUp() {
        CountDownLatch serverReady = new CountDownLatch(1);
        serverThread = new Thread(() -> {
            server = new GameServer(9000);
            server.start();
            serverReady.countDown();
        });
        serverThread.start();

        try {
            serverReady.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Server setup interrupted", e);
        }
    }

    /**
     * Tear down the server after each test.
     */
    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.shutdown();
        }
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
        }
    }

    /**
     * Test the ping functionality of the server.
     * @throws InterruptedException
     */
    @Test
    public void testPing() throws InterruptedException {
        GameClient client = new GameClient("127.0.0.1", 9000);
        client.connect();

        // Wait for a few ping intervals to ensure ping-pong messages are exchanged
        Thread.sleep(SETTINGS.Config.PING_INTERVAL.getValue() * 3L);

        // Disconnect the client
        client.disconnect();

        // Verify that the client and server handled the ping correctly
        // This can be done by checking logs or adding additional assertions if needed
    }
}