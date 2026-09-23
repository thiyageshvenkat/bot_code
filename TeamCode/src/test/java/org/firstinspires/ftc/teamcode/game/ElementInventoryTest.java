package org.firstinspires.ftc.teamcode.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ElementInventoryTest {
    @Test public void preservesFeedOrderAndCapsAtFour() {
        ElementInventory inventory = new ElementInventory(AllianceColor.BLUE);
        assertTrue(inventory.tryAdd(ScoringElement.POLLEN));
        assertTrue(inventory.tryAdd(ScoringElement.BLUE_NECTAR));
        assertTrue(inventory.tryAdd(ScoringElement.POLLEN));
        assertTrue(inventory.tryAdd(ScoringElement.POLLEN));
        assertFalse(inventory.tryAdd(ScoringElement.POLLEN));
        assertEquals(4, inventory.size());
        assertEquals(ScoringElement.POLLEN, inventory.releaseNext());
        assertEquals(ScoringElement.BLUE_NECTAR, inventory.releaseNext());
    }

    @Test public void rejectsOpposingNectarWithoutChangingCount() {
        ElementInventory inventory = new ElementInventory(AllianceColor.RED);
        assertFalse(inventory.tryAdd(ScoringElement.BLUE_NECTAR));
        assertEquals(0, inventory.size());
        assertNull(inventory.peekNext());
    }

    @Test public void reverseRejectsNewestElement() {
        ElementInventory inventory = new ElementInventory(AllianceColor.RED);
        inventory.tryAdd(ScoringElement.POLLEN);
        inventory.tryAdd(ScoringElement.RED_NECTAR);
        assertEquals(ScoringElement.RED_NECTAR, inventory.rejectNewest());
        assertEquals(ScoringElement.POLLEN, inventory.peekNext());
    }
}
