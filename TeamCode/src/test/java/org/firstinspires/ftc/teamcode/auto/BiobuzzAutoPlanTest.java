package org.firstinspires.ftc.teamcode.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
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
            assertSame(plan.startToSouthHive, plan.toShoot);
            assertNotNull(plan.toPark);
        }
    }

    @Test public void suppliedRedEndpointsArePreserved() {
        BiobuzzAutoPlan red = BiobuzzAutoPlan.forAlliance(AllianceColor.RED);
        assertPose(red.start, 56, 8, 90);
        assertPose(red.southHiveShoot, 59.25, 42, 90);
        assertPose(red.gardenCollect, 10, 10, 270);
        assertPose(red.northApproach, 31.9496, 92.4893, 270);
        assertPose(red.northCurveExit, 46.6173, 102.9301, 270);
        assertPose(red.northHiveShoot, 59.25, 102, 270);
        assertPose(red.topFlowerCollect, 48, 132, 90);
        assertPose(red.loadingZonePark, 8, 108, 180);
    }

    @Test public void everyPublicPoseStaysInsideTheFieldWithNormalizedHeading() {
        for (AllianceColor alliance : AllianceColor.values()) {
            BiobuzzAutoPlan plan = BiobuzzAutoPlan.forAlliance(alliance);
            for (Pose pose : poses(plan)) {
                assertTrue(pose.x() >= 0 && pose.x() <= BiobuzzAutoPlan.FIELD_SIZE_IN);
                assertTrue(pose.y() >= 0 && pose.y() <= BiobuzzAutoPlan.FIELD_SIZE_IN);
                assertTrue(pose.heading() >= 0 && pose.heading() < Math.PI * 2);
            }
        }
    }

    @Test public void plansAreIndependentAndNullAllianceIsRejected() {
        BiobuzzAutoPlan first = BiobuzzAutoPlan.forAlliance(AllianceColor.RED);
        BiobuzzAutoPlan second = BiobuzzAutoPlan.forAlliance(AllianceColor.RED);
        assertNotSame(first, second);
        assertNotSame(first.startToSouthHive, second.startToSouthHive);
        assertThrows(IllegalArgumentException.class,
                () -> BiobuzzAutoPlan.forAlliance(null));
    }

    private static void assertMirrored(com.pedropathing.math.Pose red,
                                       com.pedropathing.math.Pose blue) {
        assertEquals(BiobuzzAutoPlan.FIELD_SIZE_IN - red.x(), blue.x(), EPSILON);
        assertEquals(red.y(), blue.y(), EPSILON);
        double expected = ((Math.PI - red.heading()) % (2 * Math.PI) + 2 * Math.PI)
                % (2 * Math.PI);
        assertEquals(expected, blue.heading(), EPSILON);
    }

    private static void assertPose(Pose actual, double x, double y, double headingDegrees) {
        assertEquals(x, actual.x(), EPSILON);
        assertEquals(y, actual.y(), EPSILON);
        assertEquals(Math.toRadians(headingDegrees), actual.heading(), EPSILON);
    }

    private static Pose[] poses(BiobuzzAutoPlan plan) {
        return new Pose[] {
                plan.start, plan.southHiveShoot, plan.gardenCollect, plan.northApproach,
                plan.northCurveExit, plan.northHiveShoot, plan.topFlowerCollect,
                plan.loadingZonePark
        };
    }
}
