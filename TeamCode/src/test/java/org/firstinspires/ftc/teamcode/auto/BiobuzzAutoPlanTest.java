package org.firstinspires.ftc.teamcode.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.firstinspires.ftc.teamcode.game.ElementInventory.AllianceColor;
import org.junit.Test;

public class BiobuzzAutoPlanTest {
    private static final double EPSILON = 1e-9;

    @Test public void redRouteMirrorsBlueAcrossCenterline() {
        BiobuzzAutoPlan red = BiobuzzAutoPlan.forAlliance(AllianceColor.RED);
        BiobuzzAutoPlan blue = BiobuzzAutoPlan.forAlliance(AllianceColor.BLUE);

        assertMirrored(red.start, blue.start);
        assertMirrored(red.southHiveShoot, blue.southHiveShoot);
        assertMirrored(red.gardenCollect, blue.gardenCollect);
        assertMirrored(red.northApproach, blue.northApproach);
        assertMirrored(red.northCurveExit, blue.northCurveExit);
        assertMirrored(red.northHiveShoot, blue.northHiveShoot);
        assertMirrored(red.topFlowerCollect, blue.topFlowerCollect);
        assertMirrored(red.loadingZonePark, blue.loadingZonePark);
    }

    @Test public void bothAlliancesBuildShootAndParkPaths() {
        for (AllianceColor alliance : AllianceColor.values()) {
            BiobuzzAutoPlan plan = BiobuzzAutoPlan.forAlliance(alliance);
            assertNotNull(plan.startToSouthHive);
            assertNotNull(plan.southHiveToGarden);
            assertNotNull(plan.gardenToNorthApproach);
            assertNotNull(plan.northCornerCurve);
            assertNotNull(plan.northCurveToHive);
            assertNotNull(plan.northHiveToTopFlower);
            assertNotNull(plan.topFlowerToNorthHive);
            assertNotNull(plan.northHiveToLoadingZone);
        }
    }

    @Test public void suppliedRedEndpointsArePreserved() {
        BiobuzzAutoPlan red = BiobuzzAutoPlan.forAlliance(AllianceColor.RED);
        assertEquals(56.0, red.start.x(), EPSILON);
        assertEquals(8.0, red.start.y(), EPSILON);
        assertEquals(59.25, red.southHiveShoot.x(), EPSILON);
        assertEquals(42.0, red.southHiveShoot.y(), EPSILON);
        assertEquals(8.0, red.loadingZonePark.x(), EPSILON);
        assertEquals(108.0, red.loadingZonePark.y(), EPSILON);
    }

    private static void assertMirrored(com.pedropathing.math.Pose red,
                                       com.pedropathing.math.Pose blue) {
        assertEquals(BiobuzzAutoPlan.FIELD_SIZE_IN - red.x(), blue.x(), EPSILON);
        assertEquals(red.y(), blue.y(), EPSILON);
        double expected = ((Math.PI - red.heading()) % (2 * Math.PI) + 2 * Math.PI)
                % (2 * Math.PI);
        assertEquals(expected, blue.heading(), EPSILON);
    }
}
