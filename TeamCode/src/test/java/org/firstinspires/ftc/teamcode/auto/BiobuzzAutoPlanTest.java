package org.firstinspires.ftc.teamcode.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.junit.Test;

public class BiobuzzAutoPlanTest {
    private static final double EPSILON = 1e-9;

    @Test public void redRouteMirrorsBlueAcrossCenterline() {
        BiobuzzAutoPlan blue = BiobuzzAutoPlan.forAlliance(AllianceColor.BLUE);
        BiobuzzAutoPlan red = BiobuzzAutoPlan.forAlliance(AllianceColor.RED);

        assertMirrored(blue.start.x(), blue.start.y(), blue.start.heading(),
                red.start.x(), red.start.y(), red.start.heading());
        assertMirrored(blue.shoot.x(), blue.shoot.y(), blue.shoot.heading(),
                red.shoot.x(), red.shoot.y(), red.shoot.heading());
        assertMirrored(blue.park.x(), blue.park.y(), blue.park.heading(),
                red.park.x(), red.park.y(), red.park.heading());
    }

    @Test public void bothAlliancesBuildShootAndParkPaths() {
        for (AllianceColor alliance : AllianceColor.values()) {
            BiobuzzAutoPlan plan = BiobuzzAutoPlan.forAlliance(alliance);
            assertNotNull(plan.toShoot);
            assertNotNull(plan.toPark);
        }
    }

    private static void assertMirrored(double blueX, double blueY, double blueHeading,
                                       double redX, double redY, double redHeading) {
        assertEquals(-blueX, redX, EPSILON);
        assertEquals(blueY, redY, EPSILON);
        assertEquals(Math.PI - blueHeading, redHeading, EPSILON);
    }
}
