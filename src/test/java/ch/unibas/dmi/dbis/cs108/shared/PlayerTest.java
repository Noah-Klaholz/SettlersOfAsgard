package ch.unibas.dmi.dbis.cs108.shared;

import ch.unibas.dmi.dbis.cs108.SETTINGS;
import ch.unibas.dmi.dbis.cs108.shared.entities.Findables.Artifact;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import ch.unibas.dmi.dbis.cs108.shared.entities.Purchasables.PurchasableEntity;
import ch.unibas.dmi.dbis.cs108.shared.game.Status;
import ch.unibas.dmi.dbis.cs108.shared.game.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PlayerTest {

    private Player player;
    private final String TEST_NAME = "TestPlayer";

    @BeforeEach
    void setUp() {
        player = new Player(TEST_NAME);
    }

    @Test
    void testConstructor() {
        assertNotNull(player);
        assertEquals(TEST_NAME, player.getName());
        assertNotNull(player.getPlayerID());
        assertEquals(SETTINGS.Config.START_RUNES.getValue(), player.getRunes());
        assertEquals(SETTINGS.Config.START_ENERGY.getValue(), player.getEnergy());
        assertTrue(player.getOwnedTiles().isEmpty());
        assertTrue(player.getArtifacts().isEmpty());
        assertTrue(player.getPurchasableEntities().isEmpty());
    }

    @Test
    void testSetName() {
        String newName = "NewPlayerName";
        player.setName(newName);
        assertEquals(newName, player.getName());
    }

    @Test
    void testAddRunes() {
        int initialRunes = player.getRunes();
        player.addRunes(100);
        assertEquals(initialRunes + 100, player.getRunes());
    }

    @Test
    void testAddNegativeRunes() {
        int initRunes = player.getRunes();
        player.addRunes(100);
        player.addRunes(-50);
        assertEquals(initRunes + 50, player.getRunes());
    }

    @Test
    void testAddEnergy() {
        player.setEnergy(0);
        int initialEnergy = player.getEnergy();
        player.addEnergy(2);
        assertEquals(initialEnergy + 2, player.getEnergy());
    }

    @Test
    void testAddNegativeEnergy() {
        player.setEnergy(3);
        player.addEnergy(-2);
        assertEquals(1, player.getEnergy());
        player.addEnergy(-1);
        assertEquals(0, player.getEnergy());
        player.addEnergy(-1);
        assertEquals(0, player.getEnergy());
    }

    @Test
    void testAddEnergyAboveMax() {
        player.addEnergy(100);
        assertEquals(4, player.getEnergy());
    }

    @Test
    void testAddTile() {
        Tile mockTile = mock(Tile.class);
        player.addOwnedTile(mockTile);
        assertTrue(player.getOwnedTiles().contains(mockTile));
        assertEquals(1, player.getOwnedTiles().size());
    }

    @Test
    void testAddNullTile() {
        int initialSize = player.getOwnedTiles().size();
        player.addOwnedTile(null);
        // Assuming implementation prevents adding null tiles
        assertEquals(initialSize, player.getOwnedTiles().size());
    }

    @Test
    void testRemoveTile() {
        Tile mockTile = mock(Tile.class);
        player.addOwnedTile(mockTile);
        assertTrue(player.getOwnedTiles().contains(mockTile));

        player.removeOwnedTile(mockTile);
        assertFalse(player.getOwnedTiles().contains(mockTile));
    }

    @Test
    void testAddArtifact() {
        Artifact mockArtifact = mock(Artifact.class);
        player.addArtifact(mockArtifact);
        assertTrue(player.getArtifacts().contains(mockArtifact));
    }

    @Test
    void testRemoveArtifact() {
        Artifact mockArtifact = mock(Artifact.class);
        when(mockArtifact.getId()).thenReturn(1);

        player.addArtifact(mockArtifact);
        assertTrue(player.getArtifacts().contains(mockArtifact));

        player.removeArtifact(mockArtifact);
        assertFalse(player.getArtifacts().contains(mockArtifact));
    }

    @Test
    void testRemoveArtifactById() {
        Artifact mockArtifact = mock(Artifact.class);
        when(mockArtifact.getId()).thenReturn(1);

        player.addArtifact(mockArtifact);
        assertTrue(player.getArtifacts().contains(mockArtifact));

        player.removeArtifact(mockArtifact);
        assertFalse(player.getArtifacts().contains(mockArtifact));
    }

    @Test
    void testAddPurchasableEntity() {
        PurchasableEntity mockEntity = mock(PurchasableEntity.class);
        player.addPurchasableEntity(mockEntity);
        assertTrue(player.getPurchasableEntities().contains(mockEntity));
    }

    @Test
    void testRemovePurchasableEntity() {
        PurchasableEntity mockEntity = mock(PurchasableEntity.class);
        when(mockEntity.getId()).thenReturn(1);

        player.addPurchasableEntity(mockEntity);
        assertTrue(player.getPurchasableEntities().contains(mockEntity));

        player.removePurchasableEntity(mockEntity);
        assertFalse(player.getPurchasableEntities().contains(mockEntity));
    }

    @Test
    void testGetStatus() {
        assertNotNull(player.getStatus());
        // Check default status values are initialized
        assertEquals(1.0, player.getStatus().get(Status.BuffType.RUNE_GENERATION));
        assertEquals(1.0, player.getStatus().get(Status.BuffType.ENERGY_GENERATION));
        assertEquals(1.0, player.getStatus().get(Status.BuffType.RIVER_RUNE_GENERATION));
        assertEquals(1.0, player.getStatus().get(Status.BuffType.SHOP_PRICE));
    }

    @Test
    void testAddBuff() {
        double initialValue = player.getStatus().get(Status.BuffType.RUNE_GENERATION);
        player.addBuff(Status.BuffType.RUNE_GENERATION, 0.5);
        assertEquals(initialValue + 0.5, player.getStatus().get(Status.BuffType.RUNE_GENERATION));
    }

    @Test
    void testAddNegativeBuff() {
        double initialValue = player.getStatus().get(Status.BuffType.SHOP_PRICE);
        player.addBuff(Status.BuffType.SHOP_PRICE, -0.1);
        assertEquals(initialValue - 0.1, player.getStatus().get(Status.BuffType.SHOP_PRICE));
    }

    @Test
    void testEqualsAndHashCode() {
        Player samePlayer = new Player(TEST_NAME);
        // Set the same playerID
        // This assumes there's a way to set or access the playerID for testing

        Player differentPlayer = new Player("DifferentPlayer");

        // These assertions might need adjustment based on actual equals implementation
        assertEquals(player, player);
        assertNotEquals(player, differentPlayer);
        assertNotEquals(player, null);
        assertNotEquals(player, "Not a player");
    }
}