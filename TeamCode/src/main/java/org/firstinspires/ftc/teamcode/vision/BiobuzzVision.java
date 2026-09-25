package org.firstinspires.ftc.teamcode.vision;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.constants.RobotConfig;
import org.firstinspires.ftc.teamcode.game.AllianceColor;

/** Uses the robot's one Limelight for pollen detection or upward-Hive-Cell targeting. */
public final class BiobuzzVision implements AutoCloseable {
    public enum Mode {
        POLLEN,
        HIVE_APRIL_TAGS
    }

    public enum HiveCell {
        AUDIENCE_SIDE,
        NON_AUDIENCE_SIDE
    }

    public static final class PollenTarget {
        public final double bearingDegrees;
        public final double areaPercent;

        private PollenTarget(double bearingDegrees, double areaPercent) {
            this.bearingDegrees = bearingDegrees;
            this.areaPercent = areaPercent;
        }
    }

    public static final class HiveTarget {
        public final HiveCell cell;
        public final double bearingDegrees;
        public final int visibleTagCount;

        private HiveTarget(HiveCell cell, double bearingDegrees, int visibleTagCount) {
            this.cell = cell;
            this.bearingDegrees = bearingDegrees;
            this.visibleTagCount = visibleTagCount;
        }
    }

    private final Limelight3A limelight;
    private Mode mode;

    public BiobuzzVision(HardwareMap hardwareMap) {
        limelight = hardwareMap.get(Limelight3A.class, RobotConfig.Vision.LIMELIGHT);
        limelight.setPollRateHz(50);
        usePollenPipeline();
        limelight.start();
    }

    /** Selects the neural detector used while looking for loose pollen. */
    public void usePollenPipeline() {
        selectMode(Mode.POLLEN, RobotConfig.Vision.POLLEN_PIPELINE);
    }

    /** Selects the 36h11 pipeline used to identify the currently upward-facing Hive Cell. */
    public void useHiveAprilTagPipeline() {
        selectMode(Mode.HIVE_APRIL_TAGS, RobotConfig.Vision.HIVE_APRILTAG_PIPELINE);
    }

    public Mode getMode() {
        return mode;
    }

    public PollenTarget bestPollen() {
        if (mode != Mode.POLLEN) {
            return null;
        }

        LLResult result = validLatestResult();
        if (result == null) {
            return null;
        }

        PollenTarget best = null;
        for (LLResultTypes.DetectorResult detection : result.getDetectorResults()) {
            boolean correctClass = RobotConfig.Vision.POLLEN_CLASS.equals(
                    detection.getClassName());
            boolean confident = detection.getConfidence() >= RobotConfig.Vision.MIN_CONFIDENCE;
            if (!correctClass || !confident) {
                continue;
            }

            PollenTarget candidate = new PollenTarget(
                    detection.getTargetXDegrees(), detection.getTargetArea());
            if (best == null || candidate.areaPercent > best.areaPercent) {
                best = candidate;
            }
        }
        return best;
    }

    /**
     * Returns the own-alliance Cell whose underside tag cluster is visible. That is the Cell facing
     * upward and legal to shoot into. Seeing tags from both Cells is treated as ambiguous, which
     * prevents feeding while the Hive is moving between its two stable positions.
     */
    public HiveTarget upwardHiveTarget(AllianceColor alliance) {
        if (mode != Mode.HIVE_APRIL_TAGS || alliance == null) {
            return null;
        }

        LLResult result = validLatestResult();
        if (result == null) {
            return null;
        }

        HiveCell visibleCell = null;
        double bearingTotal = 0.0;
        int tagCount = 0;
        for (LLResultTypes.FiducialResult tag : result.getFiducialResults()) {
            HiveCell tagCell = cellForTag(alliance, tag.getFiducialId());
            if (tagCell == null) {
                continue;
            }
            if (visibleCell != null && visibleCell != tagCell) {
                return null;
            }

            visibleCell = tagCell;
            bearingTotal += tag.getTargetXDegrees();
            tagCount++;
        }

        if (visibleCell == null || tagCount == 0) {
            return null;
        }
        return new HiveTarget(visibleCell, bearingTotal / tagCount, tagCount);
    }

    /** Maps the official BIOBUZZ tag ranges to the Cell carrying each cluster. */
    static HiveCell cellForTag(AllianceColor alliance, int tagId) {
        if (alliance == AllianceColor.RED) {
            if (tagId >= 30 && tagId <= 33) {
                return HiveCell.NON_AUDIENCE_SIDE;
            }
            if (tagId >= 34 && tagId <= 37) {
                return HiveCell.AUDIENCE_SIDE;
            }
        } else if (alliance == AllianceColor.BLUE) {
            if (tagId >= 38 && tagId <= 41) {
                return HiveCell.AUDIENCE_SIDE;
            }
            if (tagId >= 42 && tagId <= 45) {
                return HiveCell.NON_AUDIENCE_SIDE;
            }
        }
        return null;
    }

    public boolean isConnected() {
        return limelight.isConnected();
    }

    @Override
    public void close() {
        limelight.stop();
    }

    private void selectMode(Mode nextMode, int pipelineIndex) {
        if (mode == nextMode) {
            return;
        }
        limelight.pipelineSwitch(pipelineIndex);
        mode = nextMode;
    }

    private LLResult validLatestResult() {
        LLResult result = limelight.getLatestResult();
        if (result == null || !result.isValid()) {
            return null;
        }
        return result;
    }
}
