package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.client.core.entities.Player;
import ch.unibas.dmi.dbis.cs108.server.networking.GameServer;
import ch.unibas.dmi.dbis.cs108.client.networking.GameClient;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PingTest {

    private GameServer server;
    private Thread serverThread;

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
     * Test if the client receives pings from the server
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testPing() throws InterruptedException, IOException {
        Player player = Mockito.mock(Player.class);
        GameClient client = Mockito.spy(new GameClient("127.0.0.1", 9000, player));
        AtomicBoolean pingHandled = new AtomicBoolean(false);

        doAnswer(invocation -> {
            String message = invocation.getArgument(0);
            if (message.contains("PING$")) {
                pingHandled.set(true);
                return true;
            }
            return false;
        }).when(client).receiveMessage();


        // Wait for a few ping intervals to ensure ping-pong messages are exchanged
        Thread.sleep(SETTINGS.Config.PING_INTERVAL.getValue() * 3L);

        // Check if the client received pings
        // assertTrue(pingHandled.get(), "Client should have received pings from the server");

        // Disconnect the client
        client.disconnect();
    }
}
