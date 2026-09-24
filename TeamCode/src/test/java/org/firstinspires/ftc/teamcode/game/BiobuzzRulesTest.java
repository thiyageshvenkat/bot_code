package org.firstinspires.ftc.teamcode.game;

import org.firstinspires.ftc.teamcode.game.ElementInventory.AllianceColor;
import org.firstinspires.ftc.teamcode.game.ElementInventory.ScoringElement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BiobuzzRulesTest {
    @Test public void fifthElementIsRejected() {
        assertTrue(ElementInventory.canControlAnother(3));
        assertFalse(ElementInventory.canControlAnother(4));
    }

    @Test public void opponentNectarIsRejected() {
        assertFalse(ElementInventory.canCollect(AllianceColor.RED, ScoringElement.BLUE_NECTAR, 0));
        assertTrue(ElementInventory.canCollect(AllianceColor.RED, ScoringElement.RED_NECTAR, 0));
        assertTrue(ElementInventory.canCollect(AllianceColor.BLUE, ScoringElement.POLLEN, 0));
    }
}
