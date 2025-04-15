package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.server.core.structures.Command;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.protocol.CommunicationAPI.NetworkProtocol.Commands;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Comprehensive test suite for {@link Command}, verifying command parsing,
 * validation, and edge case handling.
 */
@ExtendWith(MockitoExtension.class)
public class CommandTest {

    private final Player mockPlayer = mock(Player.class);

    /**
     * Tests construction with null message.
     * Verifies:
     * - Command is marked invalid
     * - No exceptions thrown
     */
    @Test
    void testNullMessage() {
        Command cmd = new Command(null, mockPlayer);
        assertFalse(cmd.isValid());
    }

    /**
     * Tests construction with empty message.
     * Verifies:
     * - Command is marked invalid
     * - No exceptions thrown
     */
    @Test
    void testEmptyMessage() {
        Command cmd = new Command("", mockPlayer);
        assertFalse(cmd.isValid());
    }

    /**
     * Tests construction with valid message.
     * Verifies:
     * - Command is properly parsed
     * - Arguments are correctly separated
     */
    @Test
    void testValidCommandConstruction() {
        Command cmd = new Command("JOIN$player1$lobby1", mockPlayer);
        assertEquals("JOIN", cmd.getCommand());
        assertEquals(Commands.JOIN, cmd.getCommandType());
        assertArrayEquals(new String[]{"player1", "lobby1"}, cmd.getArgs());
        assertTrue(cmd.isValid());
    }

    /**
     * Tests validation of commands with correct argument counts.
     * Verifies:
     * - Commands with exact required arguments are valid
     */
    @Test
    void testValidArgumentCounts() {
        assertTrue(new Command("ENDT$", mockPlayer).isValid());
        assertTrue(new Command("JOIN$player$lobby", mockPlayer).isValid());
        assertTrue(new Command("CHTP$player2$player3$msg", mockPlayer).isValid());
        assertTrue(new Command("USSR$1$2$3$4", mockPlayer).isValid());
    }

    /**
     * Tests validation of commands with incorrect argument counts.
     * Verifies:
     * - Commands with too few/many arguments are invalid
     */
    @Test
    void testInvalidArgumentCounts() {
        assertFalse(new Command("PING$2$2$", mockPlayer).isValid()); // Missing arg
        assertFalse(new Command("JOIN$lobby", mockPlayer).isValid()); // Missing arg
        assertFalse(new Command("CHTP$1$2$3$4", mockPlayer).isValid()); // Missing arg
        assertFalse(new Command("USSR$1$2$3", mockPlayer).isValid()); // Missing arg
        assertFalse(new Command("PING$1$extra", mockPlayer).isValid()); // Extra arg
    }

    /**
     * Tests special case commands that are always valid.
     * Verifies:
     * - OK, ERR, TEST commands are always valid regardless of arguments
     */
    @Test
    void testSpecialCaseCommands() {
        assertTrue(new Command("OK$", mockPlayer).isValid());
        assertTrue(new Command("ERR$106$message", mockPlayer).isValid());
        assertTrue(new Command("TEST$extra$args", mockPlayer).isValid());
    }

    /**
     * Tests LISTPLAYERS command variations.
     * Verifies:
     * - Both valid forms are accepted
     * - Invalid forms are rejected
     */
    @Test
    void testListPlayersVariations() {
        assertTrue(new Command("LSTP$SERVER", mockPlayer).isValid());
        assertTrue(new Command("LSTP$LOBBY$lobby1", mockPlayer).isValid());
        assertFalse(new Command("LSTP$INVALID", mockPlayer).isValid());
        assertFalse(new Command("LSTP$LOBBY", mockPlayer).isValid()); // Missing arg
    }

    /**
     * Tests command type detection.
     * Verifies:
     * - Known commands are properly identified
     * - Unknown commands are marked invalid
     */
    @Test
    void testCommandTypeDetection() {
        assertEquals(Commands.JOIN, new Command("JOIN$p$l", mockPlayer).getCommandType());
        assertEquals(Commands.PING, new Command("PING$", mockPlayer).getCommandType());
        assertNull(new Command("INVALID$cmd", mockPlayer).getCommandType());
    }

    /**
     * Tests identification of administrative commands.
     * Verifies:
     * - Administrative commands are correctly identified
     * - Game commands are correctly identified as non-administrative
     */
    @Test
    void testIsAdministrative() {
        assertTrue(new Command("JOIN$l$p", mockPlayer).isAdministrative());
        assertTrue(new Command("CHTG$msg", mockPlayer).isAdministrative());
        assertFalse(new Command("BYST$1", mockPlayer).isAdministrative());
        assertFalse(new Command("USSR$1$2$3$4", mockPlayer).isAdministrative());
    }

    /**
     * Tests command with maximum arguments.
     * Verifies:
     * - Commands with maximum allowed arguments are valid
     */
    @Test
    void testMaxArguments() {
        assertTrue(new Command("USSR$1$2$3$4", mockPlayer).isValid());
    }

    /**
     * Tests command with empty arguments.
     * Verifies:
     * - Empty arguments are handled correctly
     */
    @Test
    void testEmptyArguments() {
        Command cmd = new Command("CHTG$$", mockPlayer);
        assertFalse(cmd.isValid());
    }

    /**
     * Tests toString() representation.
     * Verifies:
     * - String representation matches input format
     */
    @Test
    void testToString() {
        assertEquals("JOIN$player$lobby", new Command("JOIN$player$lobby", mockPlayer).toString());
        assertEquals("PING$", new Command("PING$", mockPlayer).toString());
    }

    /**
     * Tests command with null player.
     * Verifies:
     * - Null player is handled gracefully
     * - Command validation still works
     */
    @Test
    void testNullPlayer() {
        Command cmd = new Command("PING$1", null);
        assertNull(cmd.getPlayer());
        assertTrue(cmd.isValid());
    }
}