package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.game.AllianceColor;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class HiveTagGeometryTest {
    @Test
    public void mapsOfficialRedHiveTagRanges() {
        assertEquals(BiobuzzVision.HiveCell.OPPOSITE_AUDIENCE_SIDE,
                HiveTagGeometry.cellForTag(AllianceColor.RED, 30));
        assertEquals(BiobuzzVision.HiveCell.OPPOSITE_AUDIENCE_SIDE,
                HiveTagGeometry.cellForTag(AllianceColor.RED, 33));
        assertEquals(BiobuzzVision.HiveCell.AUDIENCE_SIDE,
                HiveTagGeometry.cellForTag(AllianceColor.RED, 34));
        assertEquals(BiobuzzVision.HiveCell.AUDIENCE_SIDE,
                HiveTagGeometry.cellForTag(AllianceColor.RED, 37));
    }

    @Test
    public void mapsOfficialBlueHiveTagRanges() {
        assertEquals(BiobuzzVision.HiveCell.AUDIENCE_SIDE,
                HiveTagGeometry.cellForTag(AllianceColor.BLUE, 38));
        assertEquals(BiobuzzVision.HiveCell.AUDIENCE_SIDE,
                HiveTagGeometry.cellForTag(AllianceColor.BLUE, 41));
        assertEquals(BiobuzzVision.HiveCell.OPPOSITE_AUDIENCE_SIDE,
                HiveTagGeometry.cellForTag(AllianceColor.BLUE, 42));
        assertEquals(BiobuzzVision.HiveCell.OPPOSITE_AUDIENCE_SIDE,
                HiveTagGeometry.cellForTag(AllianceColor.BLUE, 45));
    }

    @Test
    public void ignoresOpponentAndUnknownTags() {
        assertNull(HiveTagGeometry.cellForTag(AllianceColor.RED, 38));
        assertNull(HiveTagGeometry.cellForTag(AllianceColor.BLUE, 34));
        assertNull(HiveTagGeometry.cellForTag(AllianceColor.RED, 99));
    }

    @Test
    public void everyTagCalculatesTheSameOpening() {
        for (int tagId = 30; tagId <= 45; tagId++) {
            AllianceColor alliance = tagId >= 38 ? AllianceColor.BLUE : AllianceColor.RED;
            double[] horizontalOffsets = {-6.5, -2.75, 2.75, 6.5};
            HiveTagGeometry.TagOpeningEstimate estimate =
                    HiveTagGeometry.estimateCellOpeningFromTag(alliance, tagId,
                            pose(horizontalOffsets[(tagId - 30) % 4],
                                    -2.8126, 54.378, 0, 0, 0));
            assertNotNull(estimate);
            assertEquals(0, estimate.openingPositionInches[0], 1e-9);
            assertEquals(-10, estimate.openingPositionInches[1], 1e-9);
            assertEquals(60, estimate.openingPositionInches[2], 1e-9);
        }

        BiobuzzVision.HiveTarget combined = HiveTagGeometry.combineMatchingTagEstimates(
                Arrays.asList(redCellOpeningEstimate(30, 0), redCellOpeningEstimate(33, 0)));
        assertNotNull(combined);
        assertEquals(0, combined.bearingDegrees, 1e-9);
        assertEquals(2, combined.visibleTagCount);
    }

    @Test
    public void rotatesTagOffsetWithTheCell() {
        double sine = Math.sin(Math.toRadians(60));
        double cosine = Math.cos(Math.toRadians(60));
        Pose3D tiltedTag = pose(5 - 6.5, -10 + cosine * 7.1874 + sine * 5.622,
                60 + sine * 7.1874 - cosine * 5.622, 0, 0, 60);
        HiveTagGeometry.TagOpeningEstimate estimate =
                HiveTagGeometry.estimateCellOpeningFromTag(
                        AllianceColor.RED, 30, tiltedTag);
        assertNotNull(estimate);
        assertEquals(5, estimate.openingPositionInches[0], 1e-9);
        assertEquals(-10, estimate.openingPositionInches[1], 1e-9);
        assertEquals(60, estimate.openingPositionInches[2], 1e-9);
    }

    @Test
    public void convertsLimelightMetersToInches() {
        Pose3D meters = new Pose3D(new Position(DistanceUnit.METER,
                -6.5 * .0254, -2.8126 * .0254, 54.378 * .0254, 0),
                new YawPitchRollAngles(AngleUnit.DEGREES, 0, 0, 0, 0));
        HiveTagGeometry.TagOpeningEstimate estimate =
                HiveTagGeometry.estimateCellOpeningFromTag(
                        AllianceColor.RED, 30, meters);
        assertNotNull(estimate);
        assertEquals(60, estimate.openingPositionInches[2], 1e-9);
    }

    @Test
    public void rejectsTagsOnDownwardOrSidewaysCells() {
        assertNull(HiveTagGeometry.estimateCellOpeningFromTag(AllianceColor.RED, 30,
                pose(0, 0, 60, 180, 0, 0)));
        assertNull(HiveTagGeometry.estimateCellOpeningFromTag(AllianceColor.RED, 30,
                pose(0, 0, 60, 90, 0, 0)));
        assertNull(HiveTagGeometry.estimateCellOpeningFromTag(AllianceColor.RED, 30,
                pose(0, 0, 60, 0, 0, 90)));
    }

    @Test
    public void rejectsMissingInvalidOrBehindCameraPosition() {
        assertNull(HiveTagGeometry.estimateCellOpeningFromTag(AllianceColor.RED, 30, null));
        assertNull(HiveTagGeometry.estimateCellOpeningFromTag(AllianceColor.RED, 30,
                pose(0, 0, 0, 0, 0, 0)));
        assertNull(HiveTagGeometry.estimateCellOpeningFromTag(AllianceColor.RED, 30,
                pose(0, 0, -1, 0, 0, 0)));
        assertNull(HiveTagGeometry.estimateCellOpeningFromTag(AllianceColor.RED, 30,
                pose(Double.NaN, 0, 60, 0, 0, 0)));
        assertNull(HiveTagGeometry.estimateCellOpeningFromTag(AllianceColor.RED, 30,
                pose(0, 0, 60, Double.NaN, 0, 0)));
    }

    @Test
    public void rejectsDifferentCellsAndMeasurementsThatAreTooFarApart() {
        assertNull(HiveTagGeometry.combineMatchingTagEstimates(Collections.emptyList()));
        assertNull(HiveTagGeometry.combineMatchingTagEstimates(Arrays.asList(
                redCellOpeningEstimate(30, 0), redCellOpeningEstimate(34, 0))));
        assertNull(HiveTagGeometry.combineMatchingTagEstimates(Arrays.asList(
                redCellOpeningEstimate(30, 0), redCellOpeningEstimate(33, 3))));
    }

    private static HiveTagGeometry.TagOpeningEstimate redCellOpeningEstimate(
            int tagId, double openingX) {
        double[] horizontalOffsets = {-6.5, -2.75, 2.75, 6.5};
        return HiveTagGeometry.estimateCellOpeningFromTag(AllianceColor.RED, tagId,
                pose(openingX + horizontalOffsets[(tagId - 30) % 4],
                        -2.8126, 54.378, 0, 0, 0));
    }

    private static Pose3D pose(
            double x, double y, double z, double yaw, double pitch, double roll) {
        return new Pose3D(new Position(DistanceUnit.INCH, x, y, z, 0),
                new YawPitchRollAngles(AngleUnit.DEGREES, yaw, pitch, roll, 0));
    }
}
