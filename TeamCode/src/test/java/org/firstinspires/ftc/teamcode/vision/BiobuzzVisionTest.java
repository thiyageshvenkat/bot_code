package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.junit.Test;

public class BiobuzzVisionTest {
    @Test
    public void mapsOfficialRedHiveTagRanges() {
        assertEquals(BiobuzzVision.HiveCell.NON_AUDIENCE_SIDE,
                BiobuzzVision.cellForTag(AllianceColor.RED, 30));
        assertEquals(BiobuzzVision.HiveCell.NON_AUDIENCE_SIDE,
                BiobuzzVision.cellForTag(AllianceColor.RED, 33));
        assertEquals(BiobuzzVision.HiveCell.AUDIENCE_SIDE,
                BiobuzzVision.cellForTag(AllianceColor.RED, 34));
        assertEquals(BiobuzzVision.HiveCell.AUDIENCE_SIDE,
                BiobuzzVision.cellForTag(AllianceColor.RED, 37));
    }

    @Test
    public void mapsOfficialBlueHiveTagRanges() {
        assertEquals(BiobuzzVision.HiveCell.AUDIENCE_SIDE,
                BiobuzzVision.cellForTag(AllianceColor.BLUE, 38));
        assertEquals(BiobuzzVision.HiveCell.AUDIENCE_SIDE,
                BiobuzzVision.cellForTag(AllianceColor.BLUE, 41));
        assertEquals(BiobuzzVision.HiveCell.NON_AUDIENCE_SIDE,
                BiobuzzVision.cellForTag(AllianceColor.BLUE, 42));
        assertEquals(BiobuzzVision.HiveCell.NON_AUDIENCE_SIDE,
                BiobuzzVision.cellForTag(AllianceColor.BLUE, 45));
    }

    @Test
    public void ignoresOpponentAndUnknownTags() {
        assertNull(BiobuzzVision.cellForTag(AllianceColor.RED, 38));
        assertNull(BiobuzzVision.cellForTag(AllianceColor.BLUE, 34));
        assertNull(BiobuzzVision.cellForTag(AllianceColor.RED, 99));
    }
}
