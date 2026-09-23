package org.firstinspires.ftc.teamcode.game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BiobuzzRulesTest {
    @Test public void fifthElementIsRejected() {
        assertTrue(BiobuzzRules.canControlAnother(3));
        assertFalse(BiobuzzRules.canControlAnother(4));
    }

    @Test public void opponentNectarIsRejected() {
        assertFalse(BiobuzzRules.canCollect(AllianceColor.RED, ScoringElement.BLUE_NECTAR, 0));
        assertTrue(BiobuzzRules.canCollect(AllianceColor.RED, ScoringElement.RED_NECTAR, 0));
        assertTrue(BiobuzzRules.canCollect(AllianceColor.BLUE, ScoringElement.POLLEN, 0));
    }

    @Test public void flowerUnlocksAtFinalMinute() {
        assertFalse(BiobuzzRules.canPlaceInFlower(ScoringElement.POLLEN, 59.99));
        assertTrue(BiobuzzRules.canPlaceInFlower(ScoringElement.POLLEN, 60.0));
        assertTrue(BiobuzzRules.canPlaceInFlower(ScoringElement.RED_NECTAR, 90.0));
    }

    @Test public void onlyPollenCanBeRetrievedFromFlower() {
        assertTrue(BiobuzzRules.canRetrieveFromFlower(ScoringElement.POLLEN));
        assertFalse(BiobuzzRules.canRetrieveFromFlower(ScoringElement.RED_NECTAR));
    }
}
