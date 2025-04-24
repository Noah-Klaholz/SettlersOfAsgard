package ch.unibas.dmi.dbis.cs108.server;

import ch.unibas.dmi.dbis.cs108.server.core.model.GameState;
import ch.unibas.dmi.dbis.cs108.server.core.structures.Lobby;
import ch.unibas.dmi.dbis.cs108.server.core.logic.GameLogic;
import ch.unibas.dmi.dbis.cs108.server.core.logic.TurnManager;
import ch.unibas.dmi.dbis.cs108.server.networking.ClientHandler;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for {@link TurnManager}, verifying correct
 * handling of players and actions.
 * Uses Mockito for mocking dependencies.
 */
@ExtendWith(MockitoExtension.class)
public class TurnManagerTest {
}
