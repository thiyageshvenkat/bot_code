package org.firstinspires.ftc.teamcode.game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BiobuzzRulesTest {
    @Test public void fifthElementIsRejected() {
        ElementInventory inventory = new ElementInventory(AllianceColor.RED);
        for (int i = 0; i < ElementInventory.CAPACITY; i++) {
            assertTrue(inventory.tryAdd(ScoringElement.POLLEN));
        }
        assertFalse(inventory.tryAdd(ScoringElement.POLLEN));
    }

    @Test public void opponentNectarIsRejected() {
        ElementInventory red = new ElementInventory(AllianceColor.RED);
        ElementInventory blue = new ElementInventory(AllianceColor.BLUE);
        assertFalse(red.tryAdd(ScoringElement.BLUE_NECTAR));
        assertTrue(red.tryAdd(ScoringElement.RED_NECTAR));
        assertTrue(blue.tryAdd(ScoringElement.POLLEN));
    }

    @Test public void everyElementAllianceCombinationMatchesTheRules() {
        for (AllianceColor alliance : AllianceColor.values()) {
            for (ScoringElement element : ScoringElement.values()) {
                boolean expected = element == ScoringElement.POLLEN
                        || alliance == AllianceColor.RED && element == ScoringElement.RED_NECTAR
                        || alliance == AllianceColor.BLUE && element == ScoringElement.BLUE_NECTAR;
                assertTrue(element + " / " + alliance,
                        element.belongsTo(alliance) == expected);
            }
        }
    }

    @Test public void noAllianceAcceptsOnlyNeutralPollen() {
        assertTrue(ScoringElement.POLLEN.belongsTo(null));
        assertFalse(ScoringElement.RED_NECTAR.belongsTo(null));
        assertFalse(ScoringElement.BLUE_NECTAR.belongsTo(null));
    }
}
