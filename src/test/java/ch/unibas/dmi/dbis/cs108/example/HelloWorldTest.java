package ch.unibas.dmi.dbis.cs108.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that the HelloWorld class actually writes "Hello World" to stdout.
 */
public class HelloWorldTest {

    /**
     * Streams to capture System.out and System.err output during the tests.
     */
    private ByteArrayOutputStream outStream;
    private ByteArrayOutputStream errStream;

    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    public void setUp() {
        // Keep track of original streams
        originalOut = System.out;
        originalErr = System.err;

        // Re-initialize capture streams each time
        outStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outStream));
        System.setErr(new PrintStream(errStream));
    }

    @AfterEach
    public void tearDown() {
        // Restore original streams
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testHelloWorldMainMethod() {
        // Call the main method
        HelloWorld.main(new String[0]);

        // Capture and normalize output
        String output = outStream.toString().replace("\r", "").replace("\n", "");

        // Verify it contains the expected text
        assertTrue(
                output.contains("Hello World"),
                "Expected 'Hello World' in the output, but got: " + output
        );
    }
}
