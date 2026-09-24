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

        for (int i = 0; i < red.poses.length; i++) assertMirrored(red.poses[i], blue.poses[i]);
    }

    @Test public void bothAlliancesBuildShootAndParkPaths() {
        for (AllianceColor alliance : AllianceColor.values()) {
            BiobuzzAutoPlan plan = BiobuzzAutoPlan.forAlliance(alliance);
            assertEquals(8, plan.route.length);
            for (com.pedropathing.paths.Path path : plan.route) assertNotNull(path);
        }
    }

    @Test public void suppliedRedEndpointsArePreserved() {
        BiobuzzAutoPlan red = BiobuzzAutoPlan.forAlliance(AllianceColor.RED);
        assertEquals(56.0, red.poses[BiobuzzAutoPlan.START].x(), EPSILON);
        assertEquals(8.0, red.poses[BiobuzzAutoPlan.START].y(), EPSILON);
        assertEquals(59.25, red.poses[BiobuzzAutoPlan.SOUTH_HIVE].x(), EPSILON);
        assertEquals(42.0, red.poses[BiobuzzAutoPlan.SOUTH_HIVE].y(), EPSILON);
        assertEquals(8.0, red.poses[BiobuzzAutoPlan.PARK].x(), EPSILON);
        assertEquals(108.0, red.poses[BiobuzzAutoPlan.PARK].y(), EPSILON);
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
