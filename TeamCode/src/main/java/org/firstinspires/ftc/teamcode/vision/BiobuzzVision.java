package org.firstinspires.ftc.teamcode.vision;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes.DetectorResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes.FiducialResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.game.AllianceColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the two vision results used by the robot.
 *
 * <p>The required launcher-aligned Limelight detects Hive AprilTags. The optional second Limelight
 * detects pollen. If the pollen camera is not installed, Hive targeting continues normally.</p>
 */
public final class BiobuzzVision implements AutoCloseable {
    /** Fixed field side occupied by the detected Cell; this does not change with robot alliance. */
    public enum HiveCell {
        AUDIENCE_SIDE,
        OPPOSITE_AUDIENCE_SIDE
    }

    /** Small set of pollen measurements needed by driver telemetry or future intake steering. */
    public static final class PollenTarget {
        // Camera-measured angle from its crosshair to the pollen. Zero is centered in the image;
        // positive means the pollen is to the camera's right and negative means it is to the left.
        public final double bearingDegrees;
        // Percentage of the whole image covered by the box drawn around the pollen. The code picks
        // the largest box as its target, but this number is not a physical distance.
        public final double areaPercent;

        private PollenTarget(double bearingDegrees, double areaPercent) {
            this.bearingDegrees = bearingDegrees;
            this.areaPercent = areaPercent;
        }
    }

    /** AprilTag result reduced to the information used for Hive aiming and telemetry. */
    public static final class HiveTarget {
        // Identifies the detected Cell using the official BIOBUZZ AprilTag IDs.
        public final HiveCell cell;
        // Angle from the camera crosshair to the estimated Cell opening. Zero is centered;
        // positive is camera-right and negative is camera-left.
        public final double bearingDegrees;
        // Number of visible tags used to calculate the Cell opening.
        public final int visibleTagCount;

        HiveTarget(HiveCell cell, double bearingDegrees, int visibleTagCount) {
            this.cell = cell;
            this.bearingDegrees = bearingDegrees;
            this.visibleTagCount = visibleTagCount;
        }
    }

    // Always present and permanently assigned to the 3D Hive AprilTag pipeline.
    private final LimelightReader hiveCamera;
    // Null when the optional second Limelight is absent from the active Robot Configuration.
    private final LimelightReader pollenCamera;

    public BiobuzzVision(HardwareMap hardwareMap) {
        // A missing Hive camera stops initialization because autonomous needs it for aiming.
        Limelight3A requiredHiveCamera = hardwareMap.get(
                Limelight3A.class, RobotConfig.Vision.HIVE_LIMELIGHT);
        hiveCamera = new LimelightReader(
                requiredHiveCamera, RobotConfig.Vision.HIVE_APRILTAG_PIPELINE);

        // The robot continues without pollen detection when the second Limelight is not installed.
        Limelight3A optionalPollenCamera = hardwareMap.tryGet(
                Limelight3A.class, RobotConfig.Vision.POLLEN_LIMELIGHT);
        pollenCamera = optionalPollenCamera == null ? null
                : new LimelightReader(optionalPollenCamera, RobotConfig.Vision.POLLEN_PIPELINE);
    }

    /**
     * Returns the pollen detection with the largest box in the newest usable image. Detections with
     * the wrong model label or confidence below MIN_CONFIDENCE are ignored. Box size helps choose
     * between visible pollen, but it is not a physical-distance measurement.
     */
    public PollenTarget largestVisiblePollen() {
        if (pollenCamera == null) {
            return null;
        }
        LLResult result = pollenCamera.getLatestUsableResult();
        if (result == null) {
            return null;
        }

        PollenTarget largestPollen = null;
        for (DetectorResult detection : result.getDetectorResults()) {
            // The neural model may contain several labels and low-confidence guesses. Only the
            // configured pollen label above the configured confidence is exposed to robot code.
            boolean isPollen = RobotConfig.Vision.POLLEN_CLASS.equals(detection.getClassName());
            boolean hasEnoughConfidence =
                    detection.getConfidence() >= RobotConfig.Vision.MIN_CONFIDENCE;
            if (!isPollen || !hasEnoughConfidence) {
                continue;
            }

            PollenTarget pollen = new PollenTarget(
                    detection.getTargetXDegrees(), detection.getTargetArea());
            if (largestPollen == null || pollen.areaPercent > largestPollen.areaPercent) {
                largestPollen = pollen;
            }
        }
        return largestPollen;
    }

    /**
     * Uses Hive AprilTags to locate our alliance's upward-facing Cell opening. Opponent tags and
     * tags on the downward-facing Cell are ignored. When several tags are visible, their separate
     * opening estimates must be close enough to combine.
     *
     * @return the estimated Cell opening, or null when no current usable estimate exists
     */
    public HiveTarget findUpwardCellOpening(AllianceColor alliance) {
        if (alliance == null) {
            return null;
        }
        LLResult result = hiveCamera.getLatestUsableResult();
        if (result == null) {
            return null;
        }

        List<HiveTagGeometry.TagOpeningEstimate> tagEstimates = new ArrayList<>();
        for (FiducialResult tag : result.getFiducialResults()) {
            // A tag marks part of a Cell, not the opening itself. Convert every visible tag to the
            // same opening position before deciding whether their measurements agree.
            HiveTagGeometry.TagOpeningEstimate estimate =
                    HiveTagGeometry.estimateCellOpeningFromTag(
                            alliance, tag.getFiducialId(), tag.getTargetPoseCameraSpace());
            if (estimate != null) {
                tagEstimates.add(estimate);
            }
        }
        return HiveTagGeometry.combineMatchingTagEstimates(tagEstimates);
    }

    /** Reports the required camera connection separately from whether it currently sees a tag. */
    public boolean isHiveCameraConnected() {
        return hiveCamera.isConnected();
    }

    /** Distinguishes an intentionally absent pollen camera from a disconnected installed camera. */
    public boolean isPollenCameraInstalled() {
        return pollenCamera != null;
    }

    /** True only when the optional pollen camera exists in the configuration and is connected. */
    public boolean isPollenCameraConnected() {
        return pollenCamera != null && pollenCamera.isConnected();
    }

    @Override
    public void close() {
        // FTC can stop an OpMode at any time; stop both cameras and their pipeline request threads.
        hiveCamera.close();
        if (pollenCamera != null) {
            pollenCamera.close();
        }
    }
}
