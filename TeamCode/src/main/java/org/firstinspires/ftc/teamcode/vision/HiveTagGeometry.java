package org.firstinspires.ftc.teamcode.vision;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.game.AllianceColor;

import java.util.List;

/**
 * Converts a BIOBUZZ Hive AprilTag measurement into the center of its Cell opening.
 *
 * <p>The AprilTags surround the opening, so aiming at a tag would not aim at the opening. This file
 * uses the official SDK 12 cluster measurements to calculate the opening from any visible tag. It
 * assumes the Hive Limelight is mounted upright and facing in the launch direction. Limelight's
 * camera axes are right, down, and forward.</p>
 */
final class HiveTagGeometry {
    // Official SDK 12 horizontal positions of the four tags relative to the Cell opening.
    private static final double[] TAG_HORIZONTAL_OFFSET_INCHES = {-6.50, -2.75, 2.75, 6.50};
    // Official up/down distance between each tag and the opening center.
    private static final double TAG_VERTICAL_OFFSET_INCHES = 7.1874;
    // Official forward/back distance between each tag and the opening center.
    private static final double TAG_DEPTH_OFFSET_INCHES = -5.622;

    private HiveTagGeometry() {}

    /**
     * Uses one tag to calculate its Cell opening. Returns null for an opponent tag, an invalid
     * camera measurement, a tag behind the camera, or a tag on the downward-facing Cell.
     */
    static TagOpeningEstimate estimateCellOpeningFromTag(
            AllianceColor alliance, int tagId, Pose3D tagPose) {
        // The tag ID identifies its alliance and which physical Cell carries it.
        BiobuzzVision.HiveCell cell = cellForTag(alliance, tagId);
        if (cell == null || tagPose == null) {
            return null;
        }

        // Limelight may report another distance unit; all official Hive measurements below use inches.
        Position tagPositionInches = tagPose.getPosition().toUnit(DistanceUnit.INCH);
        // Full 3D angles are required because the Hive pivots and the tags move with its Cells.
        YawPitchRollAngles tagAngles = tagPose.getOrientation();
        double roll = tagAngles.getRoll(AngleUnit.RADIANS);
        double pitch = tagAngles.getPitch(AngleUnit.RADIANS);
        double yaw = tagAngles.getYaw(AngleUnit.RADIANS);
        if (!hasUsablePositionAndAngles(tagPositionInches, roll, pitch, yaw)) {
            return null;
        }

        // The Cell can tip, so the tag-to-opening measurements must turn with the detected Cell.
        double[] cellRotation = calculateCellRotation(roll, pitch, yaw);
        if (!isUpwardFacingCell(cellRotation)) {
            return null;
        }
        double[] openingPositionInches = calculateOpeningPosition(
                tagId, tagPositionInches, cellRotation);
        // A calculated opening behind the camera cannot be a usable visible target.
        if (openingPositionInches[2] <= 0) {
            return null;
        }
        return new TagOpeningEstimate(cell, openingPositionInches, cellRotation);
    }

    /**
     * Averages the opening positions calculated from the visible tags. The image is rejected when
     * those tags identify different Cells or give measurements too far apart to combine safely.
     */
    static BiobuzzVision.HiveTarget combineMatchingTagEstimates(
            List<TagOpeningEstimate> tagEstimates) {
        if (tagEstimates.isEmpty()) {
            return null;
        }
        TagOpeningEstimate firstEstimate = tagEstimates.get(0);
        double[] averageOpeningPosition = new double[3];
        for (TagOpeningEstimate estimate : tagEstimates) {
            if (estimate.cell != firstEstimate.cell || areTooDifferent(estimate, firstEstimate)) {
                // Do not average different Cells or combine one likely-bad measurement with others.
                return null;
            }
            for (int axis = 0; axis < 3; axis++) {
                averageOpeningPosition[axis] +=
                        estimate.openingPositionInches[axis] / tagEstimates.size();
            }
        }
        // Horizontal angle to the opening: camera-right distance divided by forward distance.
        double bearingDegrees = Math.toDegrees(Math.atan2(
                averageOpeningPosition[0], averageOpeningPosition[2]));
        return new BiobuzzVision.HiveTarget(
                firstEstimate.cell, bearingDegrees, tagEstimates.size());
    }

    /** Maps the official tag ranges to the matching alliance Cell. */
    static BiobuzzVision.HiveCell cellForTag(AllianceColor alliance, int tagId) {
        if (alliance == AllianceColor.RED) {
            if (tagId >= 30 && tagId <= 33) {
                // Official red Cell on the side opposite the audience.
                return BiobuzzVision.HiveCell.OPPOSITE_AUDIENCE_SIDE;
            }
            if (tagId >= 34 && tagId <= 37) {
                // Official red Cell on the audience side.
                return BiobuzzVision.HiveCell.AUDIENCE_SIDE;
            }
        } else if (alliance == AllianceColor.BLUE) {
            if (tagId >= 38 && tagId <= 41) {
                // Official blue Cell on the audience side.
                return BiobuzzVision.HiveCell.AUDIENCE_SIDE;
            }
            if (tagId >= 42 && tagId <= 45) {
                // Official blue Cell on the side opposite the audience.
                return BiobuzzVision.HiveCell.OPPOSITE_AUDIENCE_SIDE;
            }
        }
        return null;
    }

    /** Rejects missing numbers and tag positions that are not in front of the camera. */
    private static boolean hasUsablePositionAndAngles(
            Position position, double roll, double pitch, double yaw) {
        return Double.isFinite(position.x) && Double.isFinite(position.y)
                && Double.isFinite(position.z) && position.z > 0
                && Double.isFinite(roll) && Double.isFinite(pitch) && Double.isFinite(yaw);
    }

    /**
     * Describes how the Cell is turned relative to the camera. The nine returned numbers let
     * calculateOpeningPosition turn the official tag-to-opening measurements the same way.
     */
    private static double[] calculateCellRotation(double roll, double pitch, double yaw) {
        double cosRoll = Math.cos(roll);
        double sinRoll = Math.sin(roll);
        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        // Apply roll, then pitch, then yaw. Each group of three values describes one camera axis.
        return new double[] {
                cosYaw * cosPitch,
                cosYaw * sinPitch * sinRoll - sinYaw * cosRoll,
                cosYaw * sinPitch * cosRoll + sinYaw * sinRoll,
                sinYaw * cosPitch,
                sinYaw * sinPitch * sinRoll + cosYaw * cosRoll,
                sinYaw * sinPitch * cosRoll - cosYaw * sinRoll,
                -sinPitch,
                cosPitch * sinRoll,
                cosPitch * cosRoll
        };
    }

    private static boolean isUpwardFacingCell(double[] cellRotation) {
        // On the upward Cell, the bottom of its tag appears below its top in an upright camera.
        // Reject the tag when that direction is reversed or almost sideways.
        double minimumDownwardDirection =
                Math.sin(Math.toRadians(RobotConfig.Vision.HIVE_UPRIGHT_MARGIN_DEGREES));
        return cellRotation[4] > minimumDownwardDirection;
    }

    private static double[] calculateOpeningPosition(
            int tagId, Position tagPositionInches, double[] cellRotation) {
        // Tags repeat in groups of four. This selects the correct horizontal measurement inside
        // whichever red or blue Cell cluster supplied the tag.
        double[] tagToOpeningOffsetInches = {
                TAG_HORIZONTAL_OFFSET_INCHES[(tagId - 30) % 4],
                TAG_VERTICAL_OFFSET_INCHES,
                TAG_DEPTH_OFFSET_INCHES
        };
        double[] openingPositionInches = {
                tagPositionInches.x, tagPositionInches.y, tagPositionInches.z
        };
        for (int positionAxis = 0; positionAxis < 3; positionAxis++) {
            for (int offsetAxis = 0; offsetAxis < 3; offsetAxis++) {
                // Rotate the official offset to match the tipped Cell, then subtract that offset
                // from the measured tag position to reach the opening center.
                openingPositionInches[positionAxis] -=
                        cellRotation[positionAxis * 3 + offsetAxis]
                                * tagToOpeningOffsetInches[offsetAxis];
            }
        }
        return openingPositionInches;
    }

    private static boolean areTooDifferent(
            TagOpeningEstimate first, TagOpeningEstimate second) {
        // Compare both the calculated opening position and the detected Cell angle. This check uses
        // tags from one image; it does not claim to determine whether the physical Hive has stopped.
        double sumOfSquaredPositionDifferences = 0;
        double rotationComparisonSum = 0;
        for (int axis = 0; axis < 3; axis++) {
            sumOfSquaredPositionDifferences += Math.pow(
                    first.openingPositionInches[axis] - second.openingPositionInches[axis], 2);
        }
        for (int value = 0; value < 9; value++) {
            rotationComparisonSum += first.cellRotation[value] * second.cellRotation[value];
        }
        double positionDifferenceInches = Math.sqrt(sumOfSquaredPositionDifferences);
        double rotationDifferenceDegrees = Math.toDegrees(Math.acos(
                Math.max(-1, Math.min(1, (rotationComparisonSum - 1) / 2))));
        return positionDifferenceInches > RobotConfig.Vision.HIVE_MAX_POSITION_DIFFERENCE_IN
                || rotationDifferenceDegrees
                > RobotConfig.Vision.HIVE_MAX_ROTATION_DIFFERENCE_DEGREES;
    }

    /** Internal calculation from one tag before measurements from several tags are combined. */
    static final class TagOpeningEstimate {
        // Cell identified by this individual tag ID.
        final BiobuzzVision.HiveCell cell;
        // Opening position calculated from this tag: camera-right, camera-down, camera-forward.
        final double[] openingPositionInches;
        // Detected Cell angle used when comparing this tag with other tags in the same image.
        final double[] cellRotation;

        TagOpeningEstimate(BiobuzzVision.HiveCell cell, double[] openingPositionInches,
                           double[] cellRotation) {
            this.cell = cell;
            this.openingPositionInches = openingPositionInches;
            this.cellRotation = cellRotation;
        }
    }
}
