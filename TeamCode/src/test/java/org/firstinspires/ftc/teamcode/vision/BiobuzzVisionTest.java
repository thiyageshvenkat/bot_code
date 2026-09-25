package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

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

    @Test
    public void everyMemberPointsToTheSameOpeningDespitePartialVisibility() {
        for (int id = 30; id <= 45; id++) {
            AllianceColor alliance = AllianceColor.RED;
            if (id >= 38) {
                alliance = AllianceColor.BLUE;
            }
            double[] xOffsets = {-6.5, -2.75, 2.75, 6.5};
            BiobuzzVision.HiveTarget target = BiobuzzVision.targetForTag(alliance, id,
                    pose(xOffsets[(id - 30) % 4], -2.8126, 54.378, 0, 0, 0), 100);
            assertNotNull(target);
            assertEquals(0, target.bearingDegrees, 1e-9);
            assertEquals(-10, target.openingInches[1], 1e-9);
            assertEquals(60, target.openingInches[2], 1e-9);
        }
        BiobuzzVision.HiveTarget first = opening(30, 0, 100);
        BiobuzzVision.HiveTarget last = opening(33, 0, 100);
        assertEquals(first.bearingDegrees,
                BiobuzzVision.selectConsistentTarget(Arrays.asList(first, last)).bearingDegrees, 1e-9);
    }

    @Test
    public void rotatesTheOffsetWithTheHiveRatherThanSubtractingAFixedCameraOffset() {
        // Independently construct a tag pitched 60 degrees about camera X around an opening.
        double sine = Math.sin(Math.toRadians(60));
        double cosine = Math.cos(Math.toRadians(60));
        Pose3D tiltedTag = pose(5 - 6.5, -10 + cosine * 7.1874 + sine * 5.622,
                60 + sine * 7.1874 - cosine * 5.622, 0, 0, 60);
        BiobuzzVision.HiveTarget target = BiobuzzVision.targetForTag(
                AllianceColor.RED, 30, tiltedTag, 100);
        assertNotNull(target);
        assertEquals(Math.toDegrees(Math.atan2(5, 60)), target.bearingDegrees, 1e-9);
        assertEquals(-10, target.openingInches[1], 1e-9);
        assertEquals(60, target.openingInches[2], 1e-9);
    }

    @Test
    public void convertsLimelightMetersToTheInchGeometry() {
        Pose3D meters = new Pose3D(new Position(DistanceUnit.METER,
                -6.5 * .0254, -2.8126 * .0254, 54.378 * .0254, 0),
                new YawPitchRollAngles(AngleUnit.DEGREES, 0, 0, 0, 0));
        BiobuzzVision.HiveTarget target = BiobuzzVision.targetForTag(AllianceColor.RED, 30, meters, 100);
        assertNotNull(target);
        assertEquals(0, target.bearingDegrees, 1e-9);
        assertEquals(60, target.openingInches[2], 1e-9);
    }

    @Test
    public void rejectsDownwardAndEdgeOnCellsEvenWhenTheirIdsAreCorrect() {
        assertNull(BiobuzzVision.targetForTag(AllianceColor.RED, 30,
                pose(0, 0, 60, 180, 0, 0), 100));
        assertNull(BiobuzzVision.targetForTag(AllianceColor.RED, 30,
                pose(0, 0, 60, 90, 0, 0), 100));
        assertNull(BiobuzzVision.targetForTag(AllianceColor.RED, 30,
                pose(0, 0, 60, 0, 0, 90), 100));
    }

    @Test
    public void rejectsMissingInvalidOrBehindCameraPose() {
        assertNull(BiobuzzVision.targetForTag(AllianceColor.RED, 30, null, 100));
        assertNull(BiobuzzVision.targetForTag(AllianceColor.RED, 30,
                pose(0, 0, 0, 0, 0, 0), 100));
        assertNull(BiobuzzVision.targetForTag(AllianceColor.RED, 30,
                pose(0, 0, -1, 0, 0, 0), 100));
        assertNull(BiobuzzVision.targetForTag(AllianceColor.RED, 30,
                pose(Double.NaN, 0, 60, 0, 0, 0), 100));
        assertNull(BiobuzzVision.targetForTag(AllianceColor.RED, 30,
                pose(0, 0, 60, Double.NaN, 0, 0), 100));
        assertNull(BiobuzzVision.targetForTag(AllianceColor.RED, 30,
                pose(0, 0, 60, 0, 0, 0), 0));
    }

    @Test
    public void rejectsConflictingCellsAndContradictoryOpeningEstimates() {
        assertNull(BiobuzzVision.selectConsistentTarget(Collections.emptyList()));
        assertNull(BiobuzzVision.selectConsistentTarget(Arrays.asList(
                opening(30, 0, 100), opening(34, 0, 100))));
        assertNull(BiobuzzVision.selectConsistentTarget(Arrays.asList(
                opening(30, 0, 100), opening(33, 3, 100))));
    }

    @Test
    public void rejectsOldPipelineAndPreSwitchFrame() {
        BiobuzzVision.FrameGate frames = new BiobuzzVision.FrameGate();
        frames.switchTo(1, 100);
        assertFalse(frames.accepts(0, 101, 0, 0));
        assertFalse(frames.accepts(1, 100, 0, 0));
        assertTrue(frames.accepts(1, 101, 0, 0));
        frames.switchTo(0, 101);
        assertFalse(frames.accepts(1, 102, 0, 0));
        assertTrue(frames.accepts(0, 102, 0, 0));
    }

    @Test
    public void rejectsStaleAndRepeatedFramesEvenWhenSdkReceiptIsFresh() {
        BiobuzzVision.FrameGate frames = new BiobuzzVision.FrameGate();
        frames.switchTo(1, Double.NaN);
        assertFalse(frames.accepts(1, 100, 200, 0));
        assertFalse(frames.accepts(1, 100, Double.NaN, 0));
        assertFalse(frames.accepts(1, 0, 0, 0));
        assertFalse(frames.accepts(1, Double.NaN, 0, 0));
        assertTrue(frames.accepts(1, 100, 0, 0));
        assertFalse(frames.accepts(1, 100, 0, 200_000_000));
        assertTrue(frames.accepts(1, 101, 0, 210_000_000));
    }

    static BiobuzzVision.HiveTarget opening(int tagId, double openingX, double timestamp) {
        double[] offsets = {-6.5, -2.75, 2.75, 6.5};
        return BiobuzzVision.targetForTag(AllianceColor.RED, tagId,
                pose(openingX + offsets[(tagId - 30) % 4], -2.8126, 54.378, 0, 0, 0), timestamp);
    }

    static Pose3D pose(double x, double y, double z, double yaw, double pitch, double roll) {
        return new Pose3D(new Position(DistanceUnit.INCH, x, y, z, 0),
                new YawPitchRollAngles(AngleUnit.DEGREES, yaw, pitch, roll, 0));
    }
}
